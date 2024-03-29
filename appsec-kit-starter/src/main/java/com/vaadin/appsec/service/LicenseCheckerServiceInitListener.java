/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.io.IOException;
import java.util.Properties;

import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * Service initialization listener to verify the license.
 */
public class LicenseCheckerServiceInitListener
        implements VaadinServiceInitListener {

    static final String PROPERTIES_RESOURCE = "appsec-kit.properties";

    static final String VERSION_PROPERTY = "appsec-kit.version";

    static final String PRODUCT_NAME = "vaadin-appsec-kit";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        final VaadinService service = event.getSource();

        try {
            final Properties properties = new Properties();
            properties.load(LicenseCheckerServiceInitListener.class
                    .getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE));
            final String version = properties.getProperty(VERSION_PROPERTY);

            UsageStatistics.markAsUsed(PRODUCT_NAME, version);

            // Check the license at runtime if in development mode
            if (!service.getDeploymentConfiguration().isProductionMode()) {
                // Using a null BuildType to allow trial licensing builds
                // The variable is defined to avoid method signature ambiguity
                BuildType buildType = null;
                LicenseChecker.checkLicense(PRODUCT_NAME, version, buildType);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
