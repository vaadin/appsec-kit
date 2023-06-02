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
import org.cyclonedx.model.Bom;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.appsec.backend.service.BillOfMaterialsStore;

import static org.mockito.Mockito.when;

public class BillOfMaterialsStoreInitListenerTest
        extends AbstractAppSecKitTest {

    @Test
    public void testBomStoreInit_initsProperly_debugMode() {
        ListAppender<ILoggingEvent> logAppender = createListAppender(
                BillOfMaterialsStoreInitializer.class.getName());

        initBomStoreInitListener(TEST_RESOURCE_BOM_PATH);

        Assert.assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        Assert.assertEquals(
                "BillOfMaterialsStoreInitListener initialization failed.",
                "BillOfMaterialsStoreInitListener initialized.",
                logAppender.list.get(0).getMessage());

        Bom bom = BillOfMaterialsStore.getInstance().getBom();
        Assert.assertNotNull(bom);
        Assert.assertEquals("Mismatch in expected dependency count.", 49,
                bom.getComponents().size());
    }

    @Test
    public void testBomStoreInit_doesNotInit_productionMode() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                BillOfMaterialsStoreInitializer.class.getName());

        initBomStoreInitListener(TEST_RESOURCE_BOM_PATH);

        Assert.assertEquals("Unexpected count of log messages. ", 0,
                logAppender.list.size());
        Assert.assertNull(BillOfMaterialsStore.getInstance().getBom());
    }

    @Test
    public void testBomStoreInit_cantGetBomResourceOnPath() {
        ListAppender<ILoggingEvent> logAppender = createListAppender(
                BillOfMaterialsStoreInitializer.class.getName());

        initBomStoreInitListener("$£@©£∞");

        Assert.assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        Assert.assertTrue(
                "'Can't get BOM resource on path' message expected but not logged.",
                logAppender.list.get(0).getMessage()
                        .startsWith("Can't get BOM resource on path"));
    }

    @Test
    public void testBomStoreInit_cantParseBomResource() {
        ListAppender<ILoggingEvent> logAppender = createListAppender(
                BillOfMaterialsStoreInitializer.class.getName());

        String pathToMalformedFile = "../../../../../logback-test.xml";
        initBomStoreInitListener(pathToMalformedFile);

        Assert.assertEquals("Unexpected count of log messages. ", 1,
                logAppender.list.size());
        Assert.assertTrue(
                "'Can't parse the BOM resource.' message expected but not logged.",
                logAppender.list.get(0).getMessage()
                        .startsWith("Can't parse the BOM resource."));
    }
}
