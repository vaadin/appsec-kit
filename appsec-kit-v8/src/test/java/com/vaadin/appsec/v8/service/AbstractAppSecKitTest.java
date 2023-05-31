/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import java.lang.reflect.Method;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinService;

import static org.mockito.Mockito.when;

public abstract class AbstractAppSecKitTest {
    public static final String TEST_RESOURCE_BOM_PATH = "../../../../../bom.json";
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected VaadinService service;

    private AutoCloseable toClose;

    @Before
    public void abstractAppSecKitTestSetup() {
        toClose = MockitoAnnotations.openMocks(this);
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);
    }

    @After
    public void tearDown() throws Exception {
        toClose.close();
    }

    protected BillOfMaterialsStoreInitListener initBomStoreInitListener(
            String pathToBom) {
        BillOfMaterialsStoreInitListener bomStoreInitListener = new BillOfMaterialsStoreInitListener();

        try {
            Method setBomPath = BillOfMaterialsStoreInitListener.class
                    .getDeclaredMethod("setBomPath", String.class);
            setBomPath.setAccessible(true);
            setBomPath.invoke(bomStoreInitListener, pathToBom);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bomStoreInitListener.serviceInit(new ServiceInitEvent(service));

        return bomStoreInitListener;
    }

    protected void initVulnStoreInitListener() {
        VulnerabilityStoreInitListener vlnStoreInitListener = new VulnerabilityStoreInitListener();
        vlnStoreInitListener.serviceInit(new ServiceInitEvent(service));
    }

    protected ListAppender<ILoggingEvent> createListAppender(
            String loggerName) {
        ListAppender<ILoggingEvent> la = new ListAppender<>();
        la.start();
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.setLevel(Level.ALL);
        logger.addAppender(la);
        return la;
    }
}
