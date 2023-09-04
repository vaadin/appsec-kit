/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.AppSecData.VulnerabilityAssessment;
import com.vaadin.appsec.backend.model.AppSecData.VulnerabilityStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AppSecServiceTest {

    static final String TEST_RESOURCE_BOM_PATH = "/bom.json";

    private AppSecConfiguration configuration;

    private Path dataFilePath;

    private Clock fixedClock;

    private AppSecService service;

    private TestScheduledExecutorService testExecutorService;

    private ObjectMapper mapper;

    @Before
    public void setup() throws Exception {
        fixedClock = Clock.fixed(Instant.ofEpochSecond(1687450676),
                ZoneOffset.UTC);
        dataFilePath = Files.createTempFile("appsec-kit", "testfile");
        AppSecService.MAPPER.writeValue(dataFilePath.toFile(),
                new AppSecData());

        testExecutorService = new TestScheduledExecutorService();

        configuration = new AppSecConfiguration();
        configuration.setDataFilePath(dataFilePath);
        configuration.setBomMavenFilePath(Paths.get(AppSecServiceTest.class
                .getResource(TEST_RESOURCE_BOM_PATH).toURI()));
        configuration.setTaskExecutor(testExecutorService);

        service = AppSecService.getInstance();
        service.setConfiguration(configuration);
        service.setClock(fixedClock);

        mapper = new ObjectMapper();
    }

    @Test(expected = AppSecException.class)
    public void serviceNotInitialized_scanForVulnerabilities_throws()
            throws Exception {
        service.scanForVulnerabilities().get();
    }

    @Test(expected = AppSecException.class)
    public void serviceNotInitialized_scheduleAutomaticScan_throws()
            throws Exception {
        service.scheduleAutomaticScan();
    }

    @Test
    public void scheduleAutomaticScan_noLastScan_noInitialDelay() {
        service.init(VaadinVersion.V8);

        service.scheduleAutomaticScan();

        assertEquals(0, testExecutorService.getLastInitialDelaySet());
    }

    @Test
    public void scheduleAutomaticScan_lastScanExists_initialDelayCalculated() {
        service.init(VaadinVersion.V8);

        // Sets the last scan to now - 23 hours
        service.getData()
                .setLastScan(fixedClock.instant().minus(23, ChronoUnit.HOURS));
        service.scheduleAutomaticScan();

        // Expect initial delay of 1 hour
        assertEquals(3600, testExecutorService.getLastInitialDelaySet());
    }

    @Test
    public void scanForVulnerabilities_lastScanUpdated() throws Exception {
        service.init(VaadinVersion.V8);

        service.scanForVulnerabilities().get();

        assertEquals(fixedClock.instant(), service.getData().getLastScan());
    }

    @Test
    public void scanForVulnerabilities_eventListenersExecuted()
            throws Exception {
        service.init(VaadinVersion.V8);

        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        service.addScanEventListener(event -> callbackExecuted.set(true));
        service.scanForVulnerabilities().get();

        assertTrue(callbackExecuted.get());
    }

    @Test
    public void newDataFile_emptyData() {
        AppSecService service = AppSecService.getInstance();
        service.setConfiguration(configuration);
        AppSecData data = service.getData();

        assertNull(data.getLastScan());
    }

    @Test
    public void existingDataFile_restoredData() {
        AppSecData existingData = createData();
        writeToFile(configuration.getDataFilePath(), existingData);

        AppSecService service = AppSecService.getInstance();
        service.setConfiguration(configuration);
        AppSecData data = service.getData();

        assertEquals(existingData.getLastScan(), data.getLastScan());
    }

    private AppSecData createData() {
        Instant now = Instant.now();
        AppSecData data = new AppSecData();
        data.setLastScan(now);
        VulnerabilityAssessment vuln = new VulnerabilityAssessment();
        vuln.setId("CVE-XXX");
        vuln.setStatus(VulnerabilityStatus.NOT_SET);
        vuln.setUpdated(now);
        vuln.setDeveloperAnalysis("TBD");
        data.getVulnerabilities().put(vuln.getId(), vuln);

        return data;
    }

    private void writeToFile(Path path, AppSecData data) {
        try {
            AppSecService.MAPPER.writeValue(path.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Can't write test file", e);
        }
    }
}
