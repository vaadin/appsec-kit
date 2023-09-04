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
import java.util.Objects;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.model.osv.response.Ecosystem;

/**
 * Provides means to store and fetch bill of materials from a static instance.
 */
class BillOfMaterialsStore {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BillOfMaterialsStore.class);

    private Bom bomMaven;
    private Bom bomNpm;

    BillOfMaterialsStore() {
    }

    Bom getBom(Ecosystem ecosystem) {
        if (Objects.requireNonNull(ecosystem) == Ecosystem.MAVEN) {
            return bomMaven;
        } else {
            return bomNpm;
        }
    }

    void readBomFile(Path bomFilePath, Ecosystem ecosystem) throws ParseException {
        if (Objects.requireNonNull(ecosystem) == Ecosystem.MAVEN) {
            bomMaven = readBomFile(bomFilePath);
        } else {
            bomNpm = readBomFile(bomFilePath);
        }
        LOGGER.debug("Reading SBOM from file " + bomFilePath.toAbsolutePath());
    }

    private Bom readBomFile(Path bomFilePath) throws ParseException {
        JsonParser parser = new JsonParser();
        File bomFile = bomFilePath.toFile();
        try {
            return parser.parse(bomFile);
        } catch (ParseException e) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            try (InputStream is = ccl
                    .getResourceAsStream(bomFilePath.toString())) {
                if (is != null) {
                    return parser.parse(is);
                } else {
                    // Throw original ParseException if resource stream is null
                    throw e;
                }
            } catch (IOException ex) {
                throw new AppSecException("SBOM file not found on path " + bomFilePath, ex);
            }
        }
    }
}
