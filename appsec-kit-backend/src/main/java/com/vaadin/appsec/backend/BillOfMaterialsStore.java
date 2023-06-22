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
import java.nio.file.Path;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;

/**
 * Provides means to store and fetch bill of materials from a static instance.
 */
class BillOfMaterialsStore {

    private Bom bom;

    BillOfMaterialsStore() {
    }

    Bom getBom() {
        return this.bom;
    }

    void readBomFile(Path bomFilePath) throws ParseException {
        JsonParser parser = new JsonParser();
        File bomFile = bomFilePath.toFile();
        bom = parser.parse(bomFile);
    }
}
