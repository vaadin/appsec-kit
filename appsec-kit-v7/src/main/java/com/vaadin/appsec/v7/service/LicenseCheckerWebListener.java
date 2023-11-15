/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.appsec.v7.service;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.server.VaadinService;

/**
 * Session listener to verify the license.
 */
@WebListener
public class LicenseCheckerWebListener implements HttpSessionListener {

    static final String PROPERTIES_RESOURCE = "appsec-kit.properties";

    static final String VERSION_PROPERTY = "appsec-kit.version";

    static final String PRODUCT_NAME = "vaadin-appsec-kit";

    private static final List<String> initializedVaadinServiceNames = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void sessionCreated(HttpSessionEvent se) {
        VaadinService vaadinService = VaadinService.getCurrent();
        if (vaadinService != null) {
            String serviceName = vaadinService.getServiceName();
            if (!initializedVaadinServiceNames.contains(serviceName)) {
                checkLicense(vaadinService);
                initializedVaadinServiceNames.add(serviceName);
            }
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // NO-OP
    }

    private void checkLicense(VaadinService service) {
        try {
            final Properties properties = new Properties();
            properties.load(LicenseCheckerWebListener.class.getClassLoader()
                    .getResourceAsStream(PROPERTIES_RESOURCE));
            final String version = properties.getProperty(VERSION_PROPERTY);

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
