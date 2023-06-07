/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import java.nio.file.Paths;

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
import com.vaadin.appsec.backend.AppSecConfiguration;
import com.vaadin.appsec.backend.AppSecService;
=======
import com.vaadin.appsec.backend.service.BillOfMaterialsStore;
import com.vaadin.appsec.backend.service.VulnerabilityStore;
import com.vaadin.server.ServiceInitEvent;
>>>>>>> fd7081c (Clear data from singletons before each test)
import com.vaadin.server.VaadinService;

public abstract class AbstractAppSecKitTest {

    static final String TEST_RESOURCE_BOM_PATH = "/bom.json";

    protected AppSecConfiguration configuration;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected VaadinService service;

    private AutoCloseable toClose;

    @Before
    public void abstractAppSecKitTestSetup() throws Exception {
        toClose = MockitoAnnotations.openMocks(this);
<<<<<<< HEAD

        configuration = new AppSecConfiguration();
        configuration.setBomFilePath(Paths.get(AbstractAppSecKitTest.class
                .getResource(TEST_RESOURCE_BOM_PATH).toURI()));
        AppSecService.getInstance().setConfiguration(configuration);
=======
        when(service.getDeploymentConfiguration().isProductionMode())
                .thenReturn(false);

        // Clear data from singletons before each test
        BillOfMaterialsStore.getInstance().init(null);
        VulnerabilityStore.getInstance().init(null);
>>>>>>> fd7081c (Clear data from singletons before each test)
    }

    @After
    public void tearDown() throws Exception {
        toClose.close();
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
