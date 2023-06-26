/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.model.dto.VulnerabilityDTO;

public class AppSecDTOProviderTest {

    static final String TEST_RESOURCE_BOM_PATH = "../../../../bom.json";

    private BillOfMaterialsStore bomStore;

    private VulnerabilityStore vulnerabilityStore;

    private OpenSourceVulnerabilityService osvService;

    @Before
    public void setup() {
        bomStore = new BillOfMaterialsStore();
        osvService = new OpenSourceVulnerabilityService();
        vulnerabilityStore = new VulnerabilityStore(osvService, bomStore);

        // Init BOM Store
        URL resource = AppSecDTOProviderTest.class
                .getResource(TEST_RESOURCE_BOM_PATH);
        try {
            bomStore.readBomFile(Paths.get(resource.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Init vulnerability store
        vulnerabilityStore.refresh();
    }

    @Test
    public void testGetDependencies() {
        AppSecDTOProvider dtoProvider = new AppSecDTOProvider(
                vulnerabilityStore, bomStore);
        List<DependencyDTO> dependencies = dtoProvider.getDependencies();

        Assert.assertEquals("Mismatch in expected dependency count.", 2,
                dependencies.size());
    }

    @Test
    public void testGetVulnerabilities() {
        AppSecDTOProvider dtoProvider = new AppSecDTOProvider(
                vulnerabilityStore, bomStore);
        List<VulnerabilityDTO> vulnerabilities = dtoProvider
                .getVulnerabilities();

        Assert.assertEquals("Mismatch in expected vulnerability count.", 3,
                vulnerabilities.size());
    }
}
