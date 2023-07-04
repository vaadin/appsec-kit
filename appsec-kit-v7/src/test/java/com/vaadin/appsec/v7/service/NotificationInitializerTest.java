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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class NotificationInitializerTest extends AbstractAppSecKitTest {

    private NotificationInitializer noitificationInitializer;

    @Before
    public void setup() {
        noitificationInitializer = new NotificationInitializer();
    }

    @Test
    public void testNotificationInitializer_initsProperly_debugMode() {
        ListAppender<ILoggingEvent> logAppender = createListAppender(
                NotificationInitializer.class.getName());

        noitificationInitializer.serviceInit(service);

        assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        assertEquals("NotificationInitListener initialization failed.",
                "Subscribed to AppSec Kit scan events",
                logAppender.list.get(0).getMessage());
    }

    @Test
    public void testNotificationInitializer_doesNotInit_productionMode() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                NotificationInitializer.class.getName());

        noitificationInitializer.serviceInit(service);

        assertEquals("Unexpected count of log messages. ", 0,
                logAppender.list.size());
    }
}