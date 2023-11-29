/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.views.AppSecView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * A Vaadin service listener for registering the AppSec Kit route and
 * initializing AppSec Kit services. Will be invoked automatically by Vaadin.
 */
public class AppSecServiceInitListener implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecServiceInitListener.class);

    private static final String APPSEC_KIT_ROUTE = "vaadin-appsec-kit";
    private static final AtomicBoolean pushWarningShown = new AtomicBoolean(
            false);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService vaadinService = event.getSource();
        if (isDebugMode(vaadinService)) {
            registerRoute();
            vaadinService.addUIInitListener(this::addAfterNavigationListener);
            AppSecService appSecService = AppSecService.getInstance();
            appSecService.init();
            LOGGER.info("AppSec Kit initialized");
            appSecService.scanForVulnerabilities()
                    .exceptionally(appSecException -> {
                        LOGGER.error("Error scanning vulnerabilities.",
                                appSecException);
                        return null;
                    }).thenRun(appSecService::scheduleAutomaticScan);
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

    private void addAfterNavigationListener(UIInitEvent event) {
        event.getUI().addAfterNavigationListener(this::checkForPush);
    }

    private void checkForPush(AfterNavigationEvent event) {
        UI ui = event.getLocationChangeEvent().getUI();
        if (isAppSecView(ui) && !canPushChanges(ui) && isActivationEnabled()) {
            ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

            boolean warningAlreadyShown = pushWarningShown.getAndSet(true);
            if (!warningAlreadyShown) {
                LOGGER.warn(
                        "Server push has been automatically enabled so updates can be shown immediately. "
                                + "Add @Push annotation on your AppShellConfigurator class to suppress this warning. "
                                + "Set automaticallyActivatePush to false in AppSecConfiguration if you want to ensure push is not automatically enabled.");
            }
        }
    }

    private boolean isAppSecView(UI ui) {
        return ui.getCurrentView() instanceof AppSecView;
    }

    private boolean canPushChanges(UI ui) {
        return ui.getPushConfiguration().getPushMode().isEnabled()
                || ui.getPollInterval() > 0;
    }

    private boolean isActivationEnabled() {
        return AppSecService.getInstance().getConfiguration()
                .isAutomaticallyActivatePush();
    }
}
