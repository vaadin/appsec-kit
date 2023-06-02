/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.appsec.v7.data.DependencyDTO;
import com.vaadin.appsec.v7.data.VulnerabilityDTO;

public class AppSecDataProviderTest extends AbstractAppSecKitTest {

    @Test
    public void testGetDependencies() {
        initBomStoreInitListener(TEST_RESOURCE_BOM_PATH);
        initVulnStoreInitListener();

        List<DependencyDTO> dependencies = AppSecDataProvider.getDependencies();

        Assert.assertEquals("Mismatch in expected dependency count.", 49,
                dependencies.size());
    }

    @Test
    public void testGetVulnerabilities() {
        initBomStoreInitListener(TEST_RESOURCE_BOM_PATH);
        initVulnStoreInitListener();

        List<VulnerabilityDTO> vulnerabilities = AppSecDataProvider
                .getVulnerabilities();

        Assert.assertEquals("Mismatch in expected vulnerability count.", 4,
                vulnerabilities.size());
    }
}
