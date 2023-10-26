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
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.base.devserver.DevToolsInterface;

import elemental.json.JsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppSecDevToolsPluginTest {

    private AppSecDevToolsPlugin appSecDevToolsPlugin;
    private DevToolsInterface devToolsInterface;
    private MockedStatic<AppSecService> appSecService;
    private AppSecService appSecServiceInstance;
    private Registration registration;

    @Captor
    private ArgumentCaptor<AppSecScanEventListener> appSecScanEventListenerCaptor;
    @Captor
    private ArgumentCaptor<String> commandCaptor;
    @Captor
    private ArgumentCaptor<JsonObject> jsonObjectCaptor;

    @BeforeEach
    public void setup() {
        appSecDevToolsPlugin = new AppSecDevToolsPlugin();
        devToolsInterface = mock(DevToolsInterface.class);

        appSecServiceInstance = mock(AppSecService.class);
        registration = mock(Registration.class);
        when(appSecServiceInstance.addScanEventListener(any()))
                .thenReturn(registration);
        var vulnerabilities = Collections
                .singletonList(new Vulnerability("CVE-000"));
        when(appSecServiceInstance.getNewVulnerabilities())
                .thenReturn(vulnerabilities);

        appSecService = mockStatic(AppSecService.class);
        appSecService.when(AppSecService::getInstance)
                .thenReturn(appSecServiceInstance);
    }

    @AfterEach
    public void cleanup() {
        appSecService.close();
    }

    @Test
    public void handleConnect_sendsCommands() {
        var vulnerabilities = Arrays.asList(new Vulnerability("CVE-001"),
                new Vulnerability("CVE-002"));
        var appSecScanEvent = mock(AppSecScanEvent.class);
        when(appSecScanEvent.getNewVulnerabilities())
                .thenReturn(vulnerabilities);

        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(devToolsInterface, times(2)).send(commandCaptor.capture(),
                jsonObjectCaptor.capture());
        var commands = commandCaptor.getAllValues();
        assertEquals("appsec-kit-init", commands.get(0));
        assertEquals("appsec-kit-scan", commands.get(1));
        var values = jsonObjectCaptor.getAllValues();
        assertNotNull(values.get(0));
        assertEquals(1, values.get(1).get("vulnerabilityCount").asNumber());

        verify(appSecServiceInstance)
                .addScanEventListener(appSecScanEventListenerCaptor.capture());
        var appSecScanEventListener = appSecScanEventListenerCaptor.getValue();
        appSecScanEventListener.scanCompleted(appSecScanEvent);

        verify(devToolsInterface, times(3)).send(commandCaptor.capture(),
                jsonObjectCaptor.capture());
        var command = commandCaptor.getValue();
        assertEquals("appsec-kit-scan", command);
        var value = jsonObjectCaptor.getValue();
        assertEquals(2, value.get("vulnerabilityCount").asNumber());
    }

    @Test
    public void handleConnect_doesntAddScanEventListener_ifAlreadyAdded() {
        appSecDevToolsPlugin.handleConnect(devToolsInterface);
        appSecDevToolsPlugin.handleConnect(devToolsInterface);

        verify(appSecServiceInstance, times(1)).addScanEventListener(any());
    }

    @Test
    public void handleDisconnect_removesScanEventListener() {
        var devToolsInterface1 = mock(DevToolsInterface.class);

        appSecDevToolsPlugin.handleConnect(devToolsInterface);
        appSecDevToolsPlugin.handleConnect(devToolsInterface1);
        appSecDevToolsPlugin.handleDisconnect(devToolsInterface);
        appSecDevToolsPlugin.handleConnect(devToolsInterface1);
        appSecDevToolsPlugin.handleDisconnect(devToolsInterface);

        verify(appSecServiceInstance, times(2)).addScanEventListener(any());
        verify(registration, times(1)).remove();
    }
}
