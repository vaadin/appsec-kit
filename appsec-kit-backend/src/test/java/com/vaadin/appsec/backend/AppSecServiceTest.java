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
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.AppSecData.Vulnerability;
import com.vaadin.appsec.backend.model.AppSecData.VulnerabilityStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppSecServiceTest {

    private AppSecConfiguration configuration;

    private Path dataFilePath;

    @Before
    public void setup() throws IOException {
        dataFilePath = Files.createTempFile("appsec-kit", "testfile");
        AppSecService.MAPPER.writeValue(dataFilePath.toFile(),
                new AppSecData());
        configuration = new AppSecConfiguration();
        configuration.setDataFilePath(dataFilePath);
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
        Vulnerability vuln = new Vulnerability();
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
