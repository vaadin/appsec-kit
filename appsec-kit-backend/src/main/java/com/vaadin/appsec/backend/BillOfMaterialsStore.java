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
import java.io.IOException;
import java.io.InputStream;
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
        File bomFile = null;
        try {
            bomFile = bomFilePath.toFile();
            bom = parser.parse(bomFile);
        } catch (ParseException e) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            try (InputStream is = ccl
                    .getResourceAsStream(bomFilePath.toString())) {
                if (is != null) {
                    byte[] data = new byte[is.available()];
                    is.read(data);
                    bom = parser.parse(data);
                } else {
                    // Throw original ParseException is resource stream is null
                    throw e;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        LOGGER.debug("Reading Bill Of Materials from file "
                + bomFile.getAbsolutePath());
    }
}
