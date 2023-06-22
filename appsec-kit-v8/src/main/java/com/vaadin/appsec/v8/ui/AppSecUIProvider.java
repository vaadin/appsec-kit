/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui;

import java.util.Set;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

/**
 * UI provider for providing the AppSecUI class for UI instantiation when the
 * request contains 'vaadin-appsec-kit' parameter and the app is running on
 * debug-mode.
 */
public class AppSecUIProvider extends UIProvider {

    public static final String VAADIN_APPSEC_KIT_URL_PARAM = "vaadin-appsec-kit";

    @Override
    public Class<? extends UI> getUIClass(
            UIClassSelectionEvent uiClassSelectionEvent) {
        Set<String> requestParameters = uiClassSelectionEvent.getRequest()
                .getParameterMap().keySet();
        VaadinService service = uiClassSelectionEvent.getService();
        if (isDebugMode(service)
                && requestParameters.contains(VAADIN_APPSEC_KIT_URL_PARAM)) {
            return AppSecUI.class;
        }
        return null;
    }

    private boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
