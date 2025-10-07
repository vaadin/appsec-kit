/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.appsec.backend.AppSecConfiguration;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.views.AppSecView;
import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.LocationChangeEvent;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.communication.PushMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSecServiceInitListenerTest {

    private AppSecServiceInitListener listener;
    private VaadinService vaadinService;
    private DeploymentConfiguration deploymentConfiguration;
    private MockedStatic<AppSecService> appSecService;
    private AppSecService appSecServiceInstance;
    private MockedStatic<RouteConfiguration> routeConfiguration;
    private RouteConfiguration routeConfigurationInstance;

    @Captor
    private ArgumentCaptor<UIInitListener> uiInitListenerCaptor;

    @Captor
    private ArgumentCaptor<AfterNavigationListener> afterNavigationListenerCaptor;

    @BeforeEach
    void setup() {
        listener = new AppSecServiceInitListener();

        deploymentConfiguration = mock(DeploymentConfiguration.class);
        vaadinService = mock(VaadinService.class);
        when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);

        appSecServiceInstance = mock(AppSecService.class);
        appSecService = mockStatic(AppSecService.class);
        appSecService.when(AppSecService::getInstance)
                .thenReturn(appSecServiceInstance);

        routeConfigurationInstance = mock(RouteConfiguration.class);
        routeConfiguration = mockStatic(RouteConfiguration.class);
        routeConfiguration.when(RouteConfiguration::forApplicationScope)
                .thenReturn(routeConfigurationInstance);
    }

    @AfterEach
    void cleanup() {
        appSecService.close();
        routeConfiguration.close();
    }

    @Test
    void debugMode_appSecServiceInitialized() {
        var pushMode = mock(PushMode.class);
        when(pushMode.isEnabled()).thenReturn(false);
        var pushConfiguration = mock(PushConfiguration.class);
        when(pushConfiguration.getPushMode()).thenReturn(pushMode);
        var ui = mock(UI.class);
        var appSecView = mock(AppSecView.class);
        when(ui.getCurrentView()).thenReturn(appSecView);
        when(ui.getPushConfiguration()).thenReturn(pushConfiguration);
        UI.setCurrent(ui);
        var locationChangeEvent = mock(LocationChangeEvent.class);
        when(locationChangeEvent.getUI()).thenReturn(ui);
        var router = mock(Router.class);
        when(locationChangeEvent.getSource()).thenReturn(router);
        when(deploymentConfiguration.isProductionMode()).thenReturn(false);
        var registration = mock(com.vaadin.flow.shared.Registration.class);
        when(vaadinService.addUIInitListener(any())).thenReturn(registration);
        when(appSecServiceInstance.getConfiguration())
                .thenReturn(new AppSecConfiguration());
        var mockVoid = mock(Void.class);
        when(appSecServiceInstance.scanForVulnerabilities())
                .thenReturn((CompletableFuture.completedFuture(mockVoid)));

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        // verifies that the AppSec Kit route is set
        verify(routeConfigurationInstance).setRoute("vaadin-appsec-kit",
                AppSecView.class);

        // verifies that a UI init listener is added
        verify(vaadinService).addUIInitListener(uiInitListenerCaptor.capture());
        var uiInitListener = uiInitListenerCaptor.getValue();
        uiInitListener.uiInit(new UIInitEvent(ui, vaadinService));

        // verifies that an after navigation listener is added
        verify(ui).addAfterNavigationListener(
                afterNavigationListenerCaptor.capture());
        var afterNavigationListener = afterNavigationListenerCaptor.getValue();
        afterNavigationListener
                .afterNavigation(new AfterNavigationEvent(locationChangeEvent));

        // verifies that the push mode is set
        verify(pushConfiguration).setPushMode(PushMode.AUTOMATIC);

        // verifies that the AppSec service is initialized
        verify(appSecServiceInstance).init();

        // verifies that the vulnerability and automatic scans are triggered
        verify(appSecServiceInstance).scanForVulnerabilities();
        verify(appSecServiceInstance).scheduleAutomaticScan();
    }

    @Test
    void productionMode_appSecServiceNotInitialized() {
        when(deploymentConfiguration.isProductionMode()).thenReturn(true);

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        verify(routeConfigurationInstance, never()).setRoute(anyString(),
                any());
        verify(vaadinService, never()).addUIInitListener(any());
        verify(appSecServiceInstance, never()).init();
        verify(appSecServiceInstance, never()).scanForVulnerabilities();
        verify(appSecServiceInstance, never()).scheduleAutomaticScan();
    }
}
