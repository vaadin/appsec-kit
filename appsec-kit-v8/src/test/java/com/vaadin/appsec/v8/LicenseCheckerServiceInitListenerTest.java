/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.appsec.v8;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class LicenseCheckerServiceInitListenerTest {

    private VaadinService service;

    private MockedStatic<LicenseChecker> licenseChecker;

    @Before
    public void setup() {
        service = mock(VaadinService.class, Answers.RETURNS_DEEP_STUBS);
        licenseChecker = mockStatic(LicenseChecker.class);
    }

    @After
    public void cleanup() {
        licenseChecker.close();
    }

    @Test
    public void developmentMode_licenseIsCheckedRuntime() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);

        final String version = getProperties().getProperty(
                LicenseCheckerServiceInitListener.VERSION_PROPERTY);

        // Assert version is in X.Y format
        assertThat(version, matchesPattern("^\\d\\.\\d.*"));

        final LicenseCheckerServiceInitListener listener = new LicenseCheckerServiceInitListener();
        listener.serviceInit(new ServiceInitEvent(service));

        // Verify the license is checked
        BuildType buildType = null;
        licenseChecker.verify(() -> LicenseChecker.checkLicense(
                LicenseCheckerServiceInitListener.PRODUCT_NAME, version,
                buildType));
    }

    @Test
    public void productionMode_licenseIsNotCheckedRuntime() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        final LicenseCheckerServiceInitListener listener = new LicenseCheckerServiceInitListener();
        listener.serviceInit(new ServiceInitEvent(service));

        licenseChecker.verifyNoInteractions();
    }

    private Properties getProperties() {
        try {
            InputStream resourceAsStream = LicenseCheckerServiceInitListener.class
                    .getClassLoader().getResourceAsStream(
                            LicenseCheckerServiceInitListener.PROPERTIES_RESOURCE);
            final Properties properties = new Properties();
            properties.load(resourceAsStream);
            return properties;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
