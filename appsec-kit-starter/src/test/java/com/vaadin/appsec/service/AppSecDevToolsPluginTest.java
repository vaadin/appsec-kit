/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecScanEventListener;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.base.devserver.DevToolsInterface;

import elemental.json.Json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppSecDevToolsPluginTest {

    private AppSecDevToolsPlugin appSecDevToolsPlugin;
    private DevToolsInterface devToolsInterface;
    private MockedStatic<AppSecService> appSecService;
    private AppSecService appSecServiceInstance;

    @Captor
    private ArgumentCaptor<AppSecScanEventListener> appSecScanEventListenerCaptor;

    @BeforeEach
    public void setup() {
        appSecDevToolsPlugin = new AppSecDevToolsPlugin();
        devToolsInterface = mock(DevToolsInterface.class);
        appSecService = mockStatic(AppSecService.class);
        appSecServiceInstance = mock(AppSecService.class);
        appSecService.when(AppSecService::getInstance)
                .thenReturn(appSecServiceInstance);
    }

    @AfterEach
    public void cleanup() {
        appSecService.close();
    }

    @Test
    public void handleConnect_sendsCommands() {
        when(appSecServiceInstance.getNewVulnerabilities())
                .thenReturn(Collections.emptyList());
        when(appSecServiceInstance.addScanEventListener(any()))
                .thenReturn(() -> {
                });

        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(devToolsInterface, times(2)).send(anyString(), any());
    }

    @Test
    public void handleConnect_addsScanEventListener() {
        when(appSecServiceInstance.getNewVulnerabilities())
                .thenReturn(Collections.emptyList());
        when(appSecServiceInstance.addScanEventListener(any()))
                .thenReturn(() -> {
                });

        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(appSecServiceInstance, times(1)).addScanEventListener(any());
        assertEquals(1, appSecDevToolsPlugin.scanEventRegistrations.size());
    }

    @Test
    public void handleConnect_doesntAddScanEventListener_ifAlreadyAdded() {
        when(appSecServiceInstance.getNewVulnerabilities())
                .thenReturn(Collections.emptyList());
        appSecDevToolsPlugin.scanEventRegistrations.put(devToolsInterface,
                () -> {
                });

        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(appSecServiceInstance, never()).addScanEventListener(any());
        assertEquals(1, appSecDevToolsPlugin.scanEventRegistrations.size());
    }

    @Test
    public void scanEventListener_sendsScanResult() {
        var appSecScanEvent = mock(AppSecScanEvent.class);
        when(appSecScanEvent.getNewVulnerabilities())
                .thenReturn(Collections.emptyList());
        when(appSecServiceInstance.getNewVulnerabilities())
                .thenReturn(Collections.emptyList());

        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(appSecServiceInstance)
                .addScanEventListener(appSecScanEventListenerCaptor.capture());
        var appSecScanEventListener = appSecScanEventListenerCaptor.getValue();
        appSecScanEventListener.scanCompleted(appSecScanEvent);

        verify(devToolsInterface, times(3)).send(anyString(), any());
    }

    @Test
    public void handleMessage_returnsTrue() {
        var result = appSecDevToolsPlugin.handleMessage("command",
                Json.createObject(), devToolsInterface);
        assertTrue(result);
    }

    @Test
    public void handleDisconnect_removesScanEventListener() {
        var registration = mock(Registration.class);
        appSecDevToolsPlugin.scanEventRegistrations.put(devToolsInterface,
                registration);

        appSecDevToolsPlugin.handleDisconnect(devToolsInterface);

        verify(registration, times(1)).remove();
        assertEquals(0, appSecDevToolsPlugin.scanEventRegistrations.size());
    }

    @Test
    public void handleDisconnect_doesntRemoveScanEventListener_ifAlreadyRemoved() {
        var devToolsInterface1 = mock(DevToolsInterface.class);
        var registration = mock(Registration.class);
        appSecDevToolsPlugin.scanEventRegistrations.put(devToolsInterface1,
                registration);

        appSecDevToolsPlugin.handleDisconnect(devToolsInterface);

        verify(registration, never()).remove();
        assertEquals(1, appSecDevToolsPlugin.scanEventRegistrations.size());
    }
}
