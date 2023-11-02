/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.analysis.VulnerabilityAnalysis;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;

/**
 * Service that provides access to all AppSec Kit features, such as
 * vulnerability scanning and analysis storage.
 */
public class AppSecService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecService.class);

    private static final String FLOW_SERVER = "flow-server";

    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static final class InstanceHolder {
        static final AppSecService instance = new AppSecService(
                new AppSecConfiguration());
    }

    /**
     * Get the AppSecService singleton instance.
     *
     * @return singleton the singleton instance
     */
    public static AppSecService getInstance() {
        return InstanceHolder.instance;
    }

    private final List<AppSecScanEventListener> scanEventListeners = new ArrayList<>();

    private final VulnerabilityStore vulnerabilityStore;

    private final BillOfMaterialsStore bomStore;

    private final AppSecDTOProvider dtoProvider;

    private final GitHubService githubService;

    private AppSecConfiguration configuration;

    private AppSecData data;

    private Clock clock = Clock.systemUTC();

    private ScheduledFuture<?> scheduledScan;

    /**
     * Creates a new instance of the service.
     *
     * @param configuration
     *            the configuration bean
     */
    private AppSecService(AppSecConfiguration configuration) {
        bomStore = new BillOfMaterialsStore();
        int osvApiRatePerSecond = configuration.getOsvApiRatePerSecond();
        OpenSourceVulnerabilityService osvService = new OpenSourceVulnerabilityService(
                osvApiRatePerSecond);
        vulnerabilityStore = new VulnerabilityStore(osvService, bomStore);
        dtoProvider = new AppSecDTOProvider(vulnerabilityStore, bomStore);
        githubService = new GitHubService();
        this.configuration = configuration;
    }

    /**
     * Initializes the service reading the SBOM file.
     */
    public void init() {
        cancelScheduledScan();

        Path bomMavenFilePath = configuration.getBomFilePath();
        try {
            bomStore.readBomFile(bomMavenFilePath, Ecosystem.MAVEN);
        } catch (ParseException e) {
            throw new AppSecException("Cannot parse the Maven SBOM file: "
                    + bomMavenFilePath.toAbsolutePath(), e);
        }

        Path bomNpmFilePath = configuration.getBomNpmFilePath();
        try {
            bomStore.readBomFile(bomNpmFilePath, Ecosystem.NPM);
        } catch (ParseException e) {
            throw new AppSecException("Cannot parse the npm SBOM file: "
                    + bomNpmFilePath.toAbsolutePath(), e);
        }

        readOrCreateDataFile();
    }

    /**
     * Gets the list of Vaadin Flow versions for which the kit provides
     * vulnerability assessments.
     *
     * @return the list of versions
     */
    public List<String> getSupportedFlowVersions() {
        Optional<Component> flowServerComponent = getFlowServerComponent();
        if (flowServerComponent.isPresent()) {
            String flowServerVersion = flowServerComponent.get().getVersion();
            if (flowServerVersion.startsWith("23.")) {
                return githubService.getFlow23Versions();
            } else if (flowServerVersion.startsWith("2.")) {
                return githubService.getFlow14Versions();
            } else {
                LOGGER.warn("Not supported flow-server version: "
                        + flowServerVersion);
            }
        } else {
            LOGGER.warn("flow-server dependency not found in Maven SBOM file");
        }
        return Collections.emptyList();
    }

    private Optional<Component> getFlowServerComponent() {
        return bomStore.getBom(Ecosystem.MAVEN).getComponents().stream()
                .filter(comp -> FLOW_SERVER.equals(comp.getName())).findFirst();
    }

    /**
     * Gets the Vaadin Security Team assessments about known vulnerability
     * coming from transitive dependencies of the current maintained Vaadin
     * versions.
     *
     * @see #getSupportedFlowVersions()
     * @return the vulnerability analysis
     */
    public VulnerabilityAnalysis getVulnerabilityAnalysis() {
        return githubService.getVulnerabilityAnalysis();
    }

    /**
     * Schedules automatic scan for vulnerabilities at a fixed rate set to the
     * value configured with
     * {@link AppSecConfiguration#setAutoScanInterval(java.time.Duration)}.
     */
    public void scheduleAutomaticScan() {
        checkForInitialization();
        long initialDelay = 0;
        long autoScanPeriod = configuration.getAutoScanInterval().getSeconds();
        Instant lastScan = data.getLastScan();
        if (lastScan != null) {
            long secondsUntilNextScan = autoScanPeriod
                    - lastScan.until(clock.instant(), ChronoUnit.SECONDS);
            if (secondsUntilNextScan > 0) {
                initialDelay = secondsUntilNextScan;
            }
        }
        LOGGER.debug("Scheduling automatic scan every " + autoScanPeriod
                + " seconds");
        scheduledScan = configuration.getTaskExecutor()
                .scheduleAtFixedRate(() -> {
                    vulnerabilityStore.refresh();
                    updateLastScanTime();
                    invokeEventListeners(new AppSecScanEvent(this));
                }, initialDelay, autoScanPeriod, TimeUnit.SECONDS);
    }

    private void cancelScheduledScan() {
        if (scheduledScan != null) {
            LOGGER.debug("Cancelling scheduled scan...");
            scheduledScan.cancel(false);
        }
    }

    /**
     * Adds a listener for scan events.
     * <p>
     * All listeners will be invoked once a scan has been performed
     * successfully.
     *
     * @param listener
     *            the listener
     * @return a registration object that can be used to remove the listener
     */
    public Registration addScanEventListener(AppSecScanEventListener listener) {
        scanEventListeners.add(listener);
        return () -> scanEventListeners.remove(listener);
    }

    /**
     * Scans the application dependencies for vulnerabilities. The scan is
     * performed against the OSV database (see
     * <a href="https://osv.dev/">osv.dev</a>).
     * <p>
     * The scan is performed asynchronously on a thread created by the
     * {@link Executor} set in the service configuration (the default is a
     * single-thread executor). A custom executor can be set with
     * {@link AppSecConfiguration#setTaskExecutor(ScheduledExecutorService)}.
     *
     * @return a future completed when the scan has ended
     */
    public CompletableFuture<Void> scanForVulnerabilities() {
        checkForInitialization();
        Executor executor = configuration.getTaskExecutor();
        return CompletableFuture
                .supplyAsync(vulnerabilityStore::refresh, executor)
                .thenRun(githubService::updateReleasesCache)
                .thenRun(githubService::updateAnalysisCache)
                .thenRun(this::updateLastScanTime)
                .thenApply(vulnerabilities -> new AppSecScanEvent(this))
                .thenAccept(this::invokeEventListeners);
    }

    private void invokeEventListeners(AppSecScanEvent event) {
        LOGGER.debug("Invoking {} scan event listeners...",
                scanEventListeners.size());
        scanEventListeners.forEach(listener -> listener.scanCompleted(event));
    }

    /**
     * Gets the list of application dependencies (including transitive).
     *
     * @return the list of dependencies
     */
    public List<Dependency> getDependencies() {
        return dtoProvider.getDependencies();
    }

    /**
     * Gets the list of vulnerabilities found in application dependencies. The
     * list is always empty before the first scan. To scan dependencies for
     * vulnerabilities see {@link #scanForVulnerabilities()}.
     *
     * @return the list of vulnerabilities
     */
    public List<Vulnerability> getVulnerabilities() {
        return dtoProvider.getVulnerabilities();
    }

    /**
     * Gets the list of new vulnerabilities. A vulnerability is considered new
     * if there is no developer assessment data for that vulnerability.
     *
     * @return the list of new vulnerabilities
     */
    public List<Vulnerability> getNewVulnerabilities() {
        return getVulnerabilities().stream().filter(this::newVulnerability)
                .collect(Collectors.toList());
    }

    private boolean newVulnerability(Vulnerability vulnerability) {
        String vulnerabilityId = vulnerability.getIdentifier();
        return !getData().getVulnerabilities().containsKey(vulnerabilityId);
    }

    /**
     * Gets the data object, reading it from the file-system if the file exists.
     *
     * @return the data object, not {@code null}
     */
    public synchronized AppSecData getData() {
        if (data == null) {
            readOrCreateDataFile();
        }
        return data;
    }

    /**
     * Sets the data object, writing it to the file-system.
     *
     * @param data
     *            the data object, not {@code null}
     */
    public synchronized void setData(AppSecData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "The data object cannot be null");
        }
        this.data = data;
        writeDataFile();
    }

    /**
     * Re-reads data from disk.
     *
     * @return the data object, not {@code null}
     */
    public synchronized AppSecData refresh() {
        data = null;
        return getData();
    }

    /**
     * Updates the last scanned timestamp to current time and writes the data to
     * disk.
     */
    private synchronized void updateLastScanTime() {
        LOGGER.debug("Updating last scan time...");
        AppSecData tempData = getData();
        tempData.setLastScan(clock.instant());
        setData(tempData);
    }

    /**
     * Gets the current configuration. Changes to the instance returned from
     * this method will not be applied until the instance is set with
     * {@link #setConfiguration(AppSecConfiguration)}.
     *
     * @return the current configuration
     */
    public AppSecConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Allows to set the configuration for this singleton instance. When a new
     * configuration is set, the service need to be initialized again with
     * {@link #init()}.
     *
     * @param configuration
     *            configuration to set
     */
    public synchronized void setConfiguration(
            AppSecConfiguration configuration) {
        this.configuration = configuration;
        this.data = null;
        cancelScheduledScan();
        LOGGER.debug("Set AppSec configuration: " + configuration);
    }

    private void checkForInitialization() {
        if (data == null || bomStore.getBom(Ecosystem.MAVEN) == null) {
            throw new AppSecException(
                    "The service has not been initialized. You should run the "
                            + "init() method after setting a new configuration");
        }
    }

    private void readOrCreateDataFile() {
        File dataFile = configuration.getDataFilePath().toFile();
        if (dataFile.exists()) {
            try {
                data = MAPPER.readValue(dataFile, AppSecData.class);
                LOGGER.debug("Reading AppSec Kit data file "
                        + dataFile.getAbsolutePath());
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot read the AppSec Kit data file: "
                                + configuration.getDataFilePath().toString(),
                        e);
            }
        } else {
            data = new AppSecData();
            LOGGER.debug("AppSec Kit data created");
        }
    }

    private void writeDataFile() {
        if (data != null) {
            File dataFile = configuration.getDataFilePath().toFile();
            try {
                MAPPER.writeValue(dataFile, data);
                LOGGER.debug("AppSec Kit data file updated "
                        + dataFile.getAbsolutePath());
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot write the AppSec Kit data file: "
                                + configuration.getDataFilePath().toString(),
                        e);
            }
        }
    }

    /* for testing purposes */
    void setClock(Clock clock) {
        this.clock = clock;
    }
}
