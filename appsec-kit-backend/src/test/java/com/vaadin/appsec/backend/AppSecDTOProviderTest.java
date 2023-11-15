/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.appsec.backend.model.osv.response.Affected;
import com.vaadin.appsec.backend.model.osv.response.Event;
import com.vaadin.appsec.backend.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.backend.model.osv.response.Package;
import com.vaadin.appsec.backend.model.osv.response.Range;
import com.vaadin.appsec.backend.model.osv.response.Reference;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppSecDTOProviderTest {

    static final String TEST_RESOURCE_BOM_PATH = "../../../../bom.json";

    private BillOfMaterialsStore bomStore;
    private VulnerabilityStore vulnerabilityStore;

    @Before
    public void setup() throws Exception {
        bomStore = new BillOfMaterialsStore();
        OpenSourceVulnerabilityService osvService = mock(
                OpenSourceVulnerabilityService.class);
        when(osvService.getVulnerabilities(anyList()))
                .thenReturn(createVulnerabilities());
        vulnerabilityStore = new VulnerabilityStore(osvService, bomStore);

        // Init BOM Store
        URL bomResource = AppSecDTOProviderTest.class
                .getResource(TEST_RESOURCE_BOM_PATH);
        try {
            assert bomResource != null;
            bomStore.readBomFile(Paths.get(bomResource.toURI()));
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
        List<Dependency> dependencies = dtoProvider.getDependencies();

        Assert.assertEquals("Mismatch in expected dependency count.", 2,
                dependencies.size());
    }

    @Test
    public void testGetVulnerabilities() {
        AppSecDTOProvider dtoProvider = new AppSecDTOProvider(
                vulnerabilityStore, bomStore);
        List<Vulnerability> vulnerabilities = dtoProvider.getVulnerabilities();

        Assert.assertEquals("Mismatch in expected vulnerability count.", 1,
                vulnerabilities.size());
    }

    private List<OpenSourceVulnerability> createVulnerabilities()
            throws Exception {
        Reference reference = new Reference(Reference.Type.WEB,
                new URI("https://wwww.reference.com"));

        Affected affected = createAffected("Maven", "org.yaml:snakeyaml",
                Arrays.asList("1.32", "1.33", "1.4"), Range.Type.ECOSYSTEM,
                new HashMap<String, String>() {
                    {
                        put("introduced", "0");
                        put("fixed", "2.0");
                    }
                });

        OpenSourceVulnerability vulnerability = createVulnerability(
                "GHSA-mjmj-j48q-9wg2", "CVE-2022-1471", reference,
                Collections.singletonList(affected));

        return Collections.singletonList(vulnerability);
    }

    private OpenSourceVulnerability createVulnerability(String id, String alias,
            Reference reference, List<Affected> affected) {
        OpenSourceVulnerability vulnerability = new OpenSourceVulnerability();
        vulnerability.setId(id);
        vulnerability.setAliases(Collections.singletonList(alias));
        vulnerability.setReferences(Collections.singletonList(reference));
        vulnerability.setAffected(affected);
        return vulnerability;
    }

    private Affected createAffected(String pkgEcosystem, String pkgName,
            List<String> versions, Range.Type type,
            Map<String, String> eventKeyValues) {
        Affected affected = new Affected();
        affected.setPackage(new Package(pkgEcosystem, pkgName));
        affected.setVersions(versions);
        affected.setRanges(
                Collections.singletonList(createRange(type, eventKeyValues)));
        return affected;
    }

    private Range createRange(Range.Type type,
            Map<String, String> eventKeyValues) {
        return new Range(type, null, createEvents(eventKeyValues), null);
    }

    private List<Event> createEvents(Map<String, String> eventKeyValues) {
        List<Event> events = new ArrayList<>();
        eventKeyValues
                .forEach((key, value) -> events.add(createEvent(key, value)));
        return events;
    }

    private Event createEvent(String eventKey, String eventValue) {
        Event event = new Event();
        event.setAdditionalProperty(eventKey, eventValue);
        return event;
    }
}
