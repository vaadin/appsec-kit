/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BillOfMaterialsStoreTest {

    static final String TEST_RESOURCE_BOM_PATH = "/bom.json";

    @Test
    public void readBomFile_componentsCountIsCorrect() throws Exception {
        BillOfMaterialsStore store = new BillOfMaterialsStore();
        store.readBomFile(Paths.get(BillOfMaterialsStoreTest.class
                .getResource(TEST_RESOURCE_BOM_PATH).toURI()));

        int components = store.getBom().getComponents().size();
        assertEquals(2, components);
    }
}
