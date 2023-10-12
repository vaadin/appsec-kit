/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.views.AppSecView;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * A Vaadin service listener for registering the AppSec Kit route and
 * initializing AppSec Kit services. Will be invoked automatically by Vaadin.
 */
public class AppSecServiceInitListener implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecServiceInitListener.class);

    private static final String APPSEC_KIT_ROUTE = "vaadin-appsec-kit";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        if (isDebugMode(event.getSource())) {
            registerRoute();
            AppSecService appSecService = AppSecService.getInstance();
            appSecService.init();
            LOGGER.info("AppSec Kit initialized");
        } else {
            LOGGER.info("AppSec Kit not enabled in production mode. Run the "
                    + "application in debug mode to initialize AppSec Kit");
        }
    }

    private void registerRoute() {
        RouteConfiguration configuration = RouteConfiguration
                .forApplicationScope();
        configuration.setRoute(APPSEC_KIT_ROUTE, AppSecView.class);
    }

    private boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
