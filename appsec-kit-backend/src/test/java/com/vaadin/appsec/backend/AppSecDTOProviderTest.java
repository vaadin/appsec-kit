/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.model.dto.VulnerabilityDTO;
import com.vaadin.appsec.backend.service.BillOfMaterialsStore;
import com.vaadin.appsec.backend.service.VulnerabilityStore;

public class AppSecDTOProviderTest {

    public static final String TEST_RESOURCE_BOM_PATH = "../../../../bom.json";

    @Before
    public void setup() {
        // Clear data from singletons before each test
        BillOfMaterialsStore.getInstance().init(null);
        VulnerabilityStore.getInstance().init(null);

        // Init BOM Store
        URL resource = AppSecDTOProviderTest.class
                .getResource(TEST_RESOURCE_BOM_PATH);
        try {
            BillOfMaterialsStore.getInstance().init(new JsonParser()
                    .parse(Paths.get(resource.toURI()).toFile()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Init vulnerability store
        VulnerabilityStore.getInstance().refresh(() -> {
        });
    }

    @Test
    public void testGetDependencies() {
        List<DependencyDTO> dependencies = AppSecDTOProvider.getDependencies();

        Assert.assertEquals("Mismatch in expected dependency count.", 2,
                dependencies.size());
    }

    @Test
    public void testGetVulnerabilities() {
        List<VulnerabilityDTO> vulnerabilities = AppSecDTOProvider
                .getVulnerabilities();

        Assert.assertEquals("Mismatch in expected vulnerability count.", 3,
                vulnerabilities.size());
    }
}
