/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AppSecKitInitializerTest extends AbstractAppSecKitTest {

    @Test
    public void debugMode_kitInitialized() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                AppSecKitInitializer.class.getName());

        AppSecKitInitializer listener = new AppSecKitInitializer();
        listener.sessionCreated(null);

        assertEquals("Unexpected count of log messages. ", 2,
                logAppender.list.size());
        assertEquals("AppSecService initialization failed.",
                "AppSecService initialized",
                logAppender.list.get(0).getMessage());
    }

    @Test
    public void productionMode_kitNotInitialized() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                AppSecKitInitializer.class.getName());

        AppSecKitInitializer listener = new AppSecKitInitializer();
        listener.sessionCreated(null);

        assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        assertEquals("AppSecService shouldn't initialize in production mode.",
                "AppSec Kit not enabled in production mode. Run the "
                        + "application in debug mode to initialize AppSec Kit",
                logAppender.list.get(0).getMessage());
    }
}