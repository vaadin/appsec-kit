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
import org.cyclonedx.model.Bom;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.appsec.backend.service.BillOfMaterialsStore;

import static org.mockito.Mockito.when;

public class BillOfMaterialsStoreProdModeTest extends AbstractAppSecKitTest {

    @Test
    public void testBomStoreInit_doesNotInit_productionMode() {
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(true);

        ListAppender<ILoggingEvent> logAppender = createListAppender(
                BillOfMaterialsStoreInitListener.class.getName());

        initBomStoreInitListener(TEST_RESOURCE_BOM_PATH);

        Assert.assertEquals("Unexpected count of log messages. ", 0,
                logAppender.list.size());
        Assert.assertNull(BillOfMaterialsStore.getInstance().getBom());
    }
}
