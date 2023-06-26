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

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.server.VaadinService;

@WebListener
public class AppSecKitInitializer implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecKitInitializer.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService != null) {
            if (isDebugMode(vaadinService)) {
                AppSecService appSecService = AppSecService.getInstance();
                appSecService.init();
                LOGGER.info("AppSecService initialized");
                appSecService.scanForVulnerabilities()
                        .thenRun(appSecService::scheduleAutomaticScan);
                LOGGER.info("AppSecService auto-scan scheduled every "
                        + appSecService.getConfiguration().getAutoScanInterval()
                                .toString());
                NotificationInitializer.serviceInit(vaadinService);
            } else {
                LOGGER.info(
                        "AppSec Kit not enabled in production mode. Run the "
                                + "application in debug mode to initialize AppSec Kit");
            }
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // NOP
    }

    private static boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
