/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.appsec.backend.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.backend.service.VulnerabilityStore;
import com.vaadin.server.ServiceInitEvent;

import static org.mockito.Mockito.when;

public class NoticationInitListenerTest extends AbstractAppSecKitTest {

    @Test
    public void testVulnStoreInit_initsProperly_debugMode() {
        ListAppender<ILoggingEvent> logAppender = createListAppender(
                NotificationInitListener.class.getName());

        NotificationInitListener notificationInitListener = new NotificationInitListener();
        notificationInitListener.serviceInit(new ServiceInitEvent(service));

        Assert.assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        Assert.assertEquals("NotificationInitListener initialization failed.",
                "NotificationInitListener initialized.",
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

        Assert.assertEquals("Unexpected count of log messages. ", 0,
                logAppender.list.size());
    }
}