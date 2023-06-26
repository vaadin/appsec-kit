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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cyclonedx.exception.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.model.dto.VulnerabilityDTO;

/**
 * Service that provides access to all AppSec Kit features, such as
 * vulnerability scanning and analysis storage.
 */
public class AppSecService {

    static final Logger LOGGER = LoggerFactory.getLogger(AppSecService.class);

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
        return AppSecService.InstanceHolder.instance;
    }

    private final List<AppSecScanEventListener> scanEventListeners = new ArrayList<>();

    private final OpenSourceVulnerabilityService osvService;

    private final VulnerabilityStore vulnerabilityStore;

    private final BillOfMaterialsStore bomStore;

    private final AppSecDTOProvider dtoProvider;

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
        osvService = new OpenSourceVulnerabilityService();
        vulnerabilityStore = new VulnerabilityStore(osvService, bomStore);
        dtoProvider = new AppSecDTOProvider(vulnerabilityStore, bomStore);
        this.configuration = configuration;
    }

    /**
     * Initializes the service reading the SBOM file.
     */
    public void init() {
        cancelScheduledScan();
        Path bomFilePath = configuration.getBomFilePath();
        try {
            bomStore.readBomFile(bomFilePath);
        } catch (ParseException e) {
            throw new AppSecException(
                    "Cannot parse the SBOM file: " + bomFilePath.toString(), e);
        }
        readOrCreateDataFile();
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
        scheduledScan = configuration.getTaskExecutor()
                .scheduleAtFixedRate(() -> {
                    vulnerabilityStore.refresh();
                    updateLastScanTime();
                    invokeEventListeners(new AppSecScanEvent(this));
                }, initialDelay, autoScanPeriod, TimeUnit.SECONDS);
    }

    private void cancelScheduledScan() {
        if (scheduledScan != null) {
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
     * performed against the OSV database (see {@link https://osv.dev/}).
     * <p>
     * The scan is performed asynchronously on a thread created by the
     * {@link Executor} set in the service configuration (the default is a
     * single-thread executor). A custom executor can be set with
     * {@link AppSecConfiguration#setTaskExecutor(Executor)}.
     *
     * @return a future completed when the scan has ended
     */
    public CompletableFuture<Void> scanForVulnerabilities() {
        checkForInitialization();
        Executor executor = configuration.getTaskExecutor();
        return CompletableFuture
                .supplyAsync(vulnerabilityStore::refresh, executor)
                .thenRun(this::updateLastScanTime)
                .thenApply(vulnerabilities -> new AppSecScanEvent(this))
                .thenAccept(this::invokeEventListeners);
    }

    private void invokeEventListeners(AppSecScanEvent event) {
        scanEventListeners.forEach(listener -> listener.scanCompleted(event));
    }

    /**
     * Gets the list of application dependencies (including transitive).
     *
     * @return the list of dependencies
     */
    public List<DependencyDTO> getDependencies() {
        return dtoProvider.getDependencies();
    }

    /**
     * Gets the list of vulnerabilities found in application dependencies. The
     * list is always empty before the first scan. To scan dependencies for
     * vulnerabilities see {@link #scanForVulnerabilities(Runnable)}.
     *
     * @return the list of vulnerabilities
     */
    public List<VulnerabilityDTO> getVulnerabilities() {
        return dtoProvider.getVulnerabilities();
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
    }

    private void checkForInitialization() {
        if (data == null) {
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
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot read the AppSec Kit data file: "
                                + configuration.getDataFilePath().toString(),
                        e);
            }
        } else {
            data = new AppSecData();
        }
    }

    private void writeDataFile() {
        if (data != null) {
            File dataFile = configuration.getDataFilePath().toFile();
            try {
                MAPPER.writeValue(dataFile, data);
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
