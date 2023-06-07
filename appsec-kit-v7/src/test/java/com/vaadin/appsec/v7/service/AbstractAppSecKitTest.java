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

<<<<<<< HEAD
<<<<<<< HEAD
import com.vaadin.appsec.backend.service.BillOfMaterialsStore;
import com.vaadin.appsec.backend.service.VulnerabilityStore;
=======
>>>>>>> dd08b2b (Add Vaadin 7 version)
=======
import com.vaadin.appsec.backend.service.BillOfMaterialsStore;
import com.vaadin.appsec.backend.service.VulnerabilityStore;
>>>>>>> fd7081c (Clear data from singletons before each test)
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
<<<<<<< HEAD
<<<<<<< HEAD

        // Clear data from singletons before each test
        BillOfMaterialsStoreInitializer.reset();
        VulnerabilityStoreInitializer.reset();
=======
>>>>>>> dd08b2b (Add Vaadin 7 version)
=======

        // Clear data from singletons before each test
        BillOfMaterialsStore.getInstance().init(null);
        VulnerabilityStore.getInstance().init(null);
>>>>>>> fd7081c (Clear data from singletons before each test)
    }

    @After
    public void tearDown() throws Exception {
        toClose.close();
    }

    protected void initBomStoreInitListener(String pathToBom) {
        try {
            Method setBomPath = BillOfMaterialsStoreInitializer.class
                    .getDeclaredMethod("setBomPath", String.class);
            setBomPath.setAccessible(true);
            setBomPath.invoke(null, pathToBom);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BillOfMaterialsStoreInitializer.serviceInit(service);
    }

    protected void initVulnStoreInitListener() {
        new VulnerabilityStoreInitializer().serviceInit(service);
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
