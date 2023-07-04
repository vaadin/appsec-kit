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

public class NotificationInitListenerTest extends AbstractAppSecKitTest {

    @Test
    public void testVulnStoreInit_initsProperly_debugMode() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                NotificationInitListener.class.getName());

        NotificationInitListener notificationInitListener = new NotificationInitListener();
        notificationInitListener.serviceInit(new ServiceInitEvent(service));

        assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        assertEquals("NotificationInitListener initialization failed.",
                "Subscribed to AppSec Kit scan events",
                logAppender.list.get(0).getMessage());
    }

    @Test
    public void testVulnStoreInit_doesNotInit_productionMode() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                NotificationInitListener.class.getName());

        NotificationInitListener notificationInitListener = new NotificationInitListener();
        notificationInitListener.serviceInit(new ServiceInitEvent(service));

        assertEquals("Unexpected count of log messages. ", 0,
                logAppender.list.size());
    }
}