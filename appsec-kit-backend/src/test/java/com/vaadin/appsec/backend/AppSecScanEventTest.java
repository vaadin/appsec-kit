/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.appsec.backend.model.dto.Vulnerability;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AppSecScanEventTest {

    private AppSecService service;

    private List<Vulnerability> vulnerabilities;

    @Before
    public void setup() {
        service = Mockito.mock(AppSecService.class);
        vulnerabilities = new ArrayList<>();
        when(service.getNewVulnerabilities()).thenReturn(vulnerabilities);
    }

    @Test
    public void newVulnerabilities_noneExpected() {
        AppSecScanEvent event = new AppSecScanEvent(service);
        List<Vulnerability> newVulnerabilities = event.getNewVulnerabilities();

        assertEquals(0, newVulnerabilities.size());
    }

    @Test
    public void newVulnerabilities_oneExpected() {
        vulnerabilities.add(new Vulnerability("foo"));

        AppSecScanEvent event = new AppSecScanEvent(service);
        List<Vulnerability> newVulnerabilities = event.getNewVulnerabilities();

        assertEquals(1, newVulnerabilities.size());
        assertEquals("foo", newVulnerabilities.get(0).getIdentifier());
    }
}
