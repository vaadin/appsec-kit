/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.vaadin.appsec.backend.AppSecConfiguration;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppSecServiceInitListenerTest {

    private AppSecServiceInitListener listener;
    private VaadinService vaadinService;
    private DeploymentConfiguration deploymentConfiguration;
    private MockedStatic<AppSecService> appSecService;
    private AppSecService appSecServiceInstance;
    private MockedStatic<RouteConfiguration> routeConfiguration;

    @BeforeEach
    public void setup() {
        listener = new AppSecServiceInitListener();

        deploymentConfiguration = mock(DeploymentConfiguration.class);
        vaadinService = mock(VaadinService.class);
        when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);

        var appSecConfiguration = mock(AppSecConfiguration.class);
        appSecServiceInstance = mock(AppSecService.class);
        when(appSecServiceInstance.getConfiguration())
                .thenReturn(appSecConfiguration);

        appSecService = mockStatic(AppSecService.class);
        appSecService.when(AppSecService::getInstance)
                .thenReturn(appSecServiceInstance);

        var routeConfigurationInstance = mock(RouteConfiguration.class);
        routeConfiguration = mockStatic(RouteConfiguration.class);
        routeConfiguration.when(RouteConfiguration::forApplicationScope)
                .thenReturn(routeConfigurationInstance);
    }

    @AfterEach
    public void cleanup() {
        appSecService.close();
        routeConfiguration.close();
    }

    @Test
    public void debugMode_appSecServiceInitialized() {
        when(deploymentConfiguration.isProductionMode()).thenReturn(false);

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        verify(appSecServiceInstance, times(1)).init();
    }

    @Test
    public void productionMode_appSecServiceNotInitialized() {
        when(deploymentConfiguration.isProductionMode()).thenReturn(true);

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        verify(appSecServiceInstance, never()).init();
    }
}
