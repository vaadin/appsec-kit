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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.service.BillOfMaterialsStore;
import com.vaadin.server.VaadinService;

/**
 * A class for initializing the bill of materials store.
 */
public class BillOfMaterialsStoreInitializer extends AbstractInitializer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BillOfMaterialsStoreInitializer.class);
    private static String BOM_PATH = "/resources/bom.json";

    private static boolean initialized;

    /**
     * Initializes the bill of materials store service
     *
     * @param vaadinService
     *            current VaadinService instance
     */
    static void serviceInit(VaadinService vaadinService) {
        if (isDebugMode(vaadinService) && !initialized) {
            URL resource = BillOfMaterialsStoreInitializer.class
                    .getResource(BOM_PATH);
            if (resource != null) {
                JsonParser parser = new JsonParser();
                try {
                    Bom bom = parser
                            .parse(Paths.get(resource.toURI()).toFile());
                    BillOfMaterialsStore.getInstance().init(bom);
                    initialized = true;
                    LOGGER.debug(
                            "BillOfMaterialsStoreInitListener initialized.");
                } catch (URISyntaxException e) {
                    LOGGER.error(
                            "Syntax error in BOM resource path: " + BOM_PATH,
                            e);
                } catch (ParseException e) {
                    LOGGER.error("Can't parse the BOM resource.", e);
                }
            } else {
                LOGGER.error("Can't get BOM resource on path: " + BOM_PATH);
            }
        }
    }

    /* Added for testing purposes */
    private static void setBomPath(String bomPath) {
        BOM_PATH = bomPath;
    }
}
