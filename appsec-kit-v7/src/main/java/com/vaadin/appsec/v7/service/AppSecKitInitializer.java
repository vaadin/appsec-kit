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
import java.util.HashSet;
import java.util.Set;

import com.vaadin.server.VaadinService;

@WebListener
public class AppSecKitInitializer implements HttpSessionListener {

    private static final Set<String> initializedServiceNames = new HashSet<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService != null && !initializedServiceNames
                .contains(vaadinService.getServiceName())) {
            BillOfMaterialsStoreInitializer.serviceInit(vaadinService);
            VulnerabilityStoreInitializer.serviceInit(vaadinService);
            NotificationInitializer.serviceInit(vaadinService);
            initializedServiceNames.add(vaadinService.getServiceName());
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // NOP
    }
}
