/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import com.vaadin.server.VaadinService;

public class AppSecUtil {

    /**
     * Checks if the application is deployed in debug mode.
     *
     * @param vaadinService
     *            service instance to check
     * @return true if application is in debug mode
     */
    public static boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
