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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides means to store and fetch bill of materials from a static instance.
 */
class BillOfMaterialsStore {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BillOfMaterialsStore.class);

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
        LOGGER.debug("Reading the BOM from " + bomFile.getAbsolutePath()
                + " file finished");
    }
}
