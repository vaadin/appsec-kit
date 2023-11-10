/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.appsec.backend.AppSecConfiguration;
import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecScanEventListener;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionDestroyListener;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationInitListenerTest {

    private NotificationInitListener listener;
    private VaadinService vaadinService;
    private DeploymentConfiguration deploymentConfiguration;
    private MockedStatic<AppSecService> appSecService;
    private AppSecService appSecServiceInstance;
    private MockedConstruction<Notification> notification;

    @Captor
    private ArgumentCaptor<UIInitListener> uiInitListenerCaptor;
    @Captor
    private ArgumentCaptor<SessionDestroyListener> sessionDestroyListenerCaptor;
    @Captor
    private ArgumentCaptor<AppSecScanEventListener> appSecScanEventListenerCaptor;
    @Captor
    private ArgumentCaptor<Command> commandCaptor;

    @BeforeEach
    public void setup() {
        listener = new NotificationInitListener();

        deploymentConfiguration = mock(DeploymentConfiguration.class);
        vaadinService = mock(VaadinService.class);
        when(vaadinService.getDeploymentConfiguration())
                .thenReturn(deploymentConfiguration);

        appSecServiceInstance = mock(AppSecService.class);
        appSecService = mockStatic(AppSecService.class);
        appSecService.when(AppSecService::getInstance)
                .thenReturn(appSecServiceInstance);

        notification = mockConstruction(Notification.class);
    }

    @AfterEach
    public void cleanup() {
        appSecService.close();
        notification.close();
    }

    @Test
    public void debugMode_notificationInitialized() {
        var ui = mock(UI.class);
        UI.setCurrent(ui);
        var vaadinSession = mock(VaadinSession.class);
        when(vaadinSession.getUIs()).thenReturn(Collections.singletonList(ui));
        var vulnerabilities = Arrays.asList(new Vulnerability("CVE-001"),
                new Vulnerability("CVE-002"));
        var appSecScanEvent = mock(AppSecScanEvent.class);
        when(appSecScanEvent.getNewVulnerabilities())
                .thenReturn(vulnerabilities);
        when(deploymentConfiguration.isProductionMode()).thenReturn(false);
        var registration = mock(com.vaadin.flow.shared.Registration.class);
        when(vaadinService.addUIInitListener(any())).thenReturn(registration);
        when(vaadinService.addSessionDestroyListener(any()))
                .thenReturn(registration);
        var scanEventRegistration = mock(
                com.vaadin.appsec.backend.Registration.class);
        when(appSecServiceInstance.addScanEventListener(any()))
                .thenReturn(scanEventRegistration);
        var mockVoid = mock(Void.class);
        when(appSecServiceInstance.scanForVulnerabilities())
                .thenReturn((CompletableFuture.completedFuture(mockVoid)));
        when(appSecServiceInstance.getConfiguration())
                .thenReturn(new AppSecConfiguration());

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        // verifies that a UI init listener is added
        verify(vaadinService).addUIInitListener(uiInitListenerCaptor.capture());
        var uiInitListener = uiInitListenerCaptor.getValue();
        uiInitListener.uiInit(new UIInitEvent(ui, vaadinService));

        // verifies that a scan event listener is added
        verify(appSecServiceInstance)
                .addScanEventListener(appSecScanEventListenerCaptor.capture());
        var appSecScanEventListener = appSecScanEventListenerCaptor.getValue();
        appSecScanEventListener.scanCompleted(appSecScanEvent);

        // verifies that a UI is notified if new vulnerabilities are found
        verify(ui).access(commandCaptor.capture());
        var command = commandCaptor.getValue();
        command.execute();
        var constructedList = notification.constructed();
        var constructed = constructedList.get(0);
        verify(appSecScanEvent, times(2)).getNewVulnerabilities();
        assertEquals(1, constructedList.size());
        verify(constructed, times(1)).open();

        // verifies that a session destroy listener is added and
        // the scan event registration is removed when the session is destroyed
        verify(vaadinService).addSessionDestroyListener(
                sessionDestroyListenerCaptor.capture());
        var sessionDestroyListener = sessionDestroyListenerCaptor.getValue();
        sessionDestroyListener.sessionDestroy(
                new SessionDestroyEvent(vaadinService, vaadinSession));
        verify(scanEventRegistration, times(1)).remove();

        // verifies that vulnerability and automatic scans are triggered
        verify(appSecServiceInstance, times(1)).scanForVulnerabilities();
        verify(appSecServiceInstance, times(1)).scheduleAutomaticScan();
    }

    @Test
    public void productionMode_notificationNotInitialized() {
        when(deploymentConfiguration.isProductionMode()).thenReturn(true);

        listener.serviceInit(new ServiceInitEvent(vaadinService));

        verify(vaadinService, never()).addUIInitListener(any());
        verify(vaadinService, never()).addSessionDestroyListener(any());
        verify(appSecServiceInstance, never()).scanForVulnerabilities();
        verify(appSecServiceInstance, never()).scheduleAutomaticScan();
    }
}
