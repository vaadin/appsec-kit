/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Test;

import com.vaadin.server.ServiceInitEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AppSecServiceInitListenerTest extends AbstractAppSecKitTest {

    @Test
    public void debugMode_kitInitialized() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                AppSecServiceInitListener.class.getName());

        AppSecServiceInitListener listener = new AppSecServiceInitListener();
        listener.serviceInit(new ServiceInitEvent(service));

        assertEquals("Unexpected count of log messages. ", 2,
                logAppender.list.size());
        assertEquals("AppSec Kit initialization failed.",
                "AppSec Kit initialized", logAppender.list.get(0).getMessage());
    }

    @Test
    public void productionMode_kitNotInitialized() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                AppSecServiceInitListener.class.getName());

        AppSecServiceInitListener listener = new AppSecServiceInitListener();
        listener.serviceInit(new ServiceInitEvent(service));

        assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        assertEquals("AppSec Kit shouldn't initialize in production mode.",
                "AppSec Kit not enabled in production mode. Run the "
                        + "application in debug mode to initialize AppSec Kit",
                logAppender.list.get(0).getMessage());
    }
}