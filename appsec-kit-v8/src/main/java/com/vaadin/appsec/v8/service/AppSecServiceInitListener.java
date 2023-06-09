/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.server.ServiceInitEvent;

/**
 * A Vaadin service listener for initializing AppSec Kit services. Will be
 * invoked automatically by Vaadin.
 */
public class AppSecServiceInitListener extends AbstractInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecServiceInitListener.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        if (isDebugMode(event.getSource())) {
            AppSecService appSecService = AppSecService.getInstance();
            appSecService.init();
            LOGGER.info("AppSec Kit initialized");
            appSecService.scanForVulnerabilities()
                    .thenRun(appSecService::scheduleAutomaticScan);
        } else {
            LOGGER.info("AppSec Kit not enabled in production mode. Run the "
                    + "application in debug mode to initialize AppSec Kit");
        }
    }
}
