/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;
import com.vaadin.flow.component.dependency.JsModule;

import elemental.json.Json;
import elemental.json.JsonObject;

@JsModule(value = "./appsec-kit/appsec-kit-plugin.ts", developmentOnly = true)
public class AppSecDevToolsPlugin implements DevToolsMessageHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecDevToolsPlugin.class);

    private final Map<DevToolsInterface, Registration> scanEventRegistrations = new ConcurrentHashMap<>();

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        devToolsInterface.send("appsec-kit-init", Json.createObject());
        var appSecService = AppSecService.getInstance();

        if (!scanEventRegistrations.containsKey(devToolsInterface)) {
            var registration = appSecService.addScanEventListener(event -> {
                var vulnerabilityCount = event.getNewVulnerabilities().size();
                sendScanResult(vulnerabilityCount, devToolsInterface);
                LOGGER.debug(
                        "Scan event received. Vulnerabilities sent to the client: "
                                + vulnerabilityCount);
            });
            scanEventRegistrations.put(devToolsInterface, registration);
            LOGGER.debug("Scan event listener added");
        }
        sendScanResult(appSecService.getNewVulnerabilities().size(),
                devToolsInterface);
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        return false;
    }

    @Override
    public void handleDisconnect(DevToolsInterface devToolsInterface) {
        var registration = scanEventRegistrations.remove(devToolsInterface);
        if (registration != null) {
            registration.remove();
            LOGGER.debug("Scan event listener removed");
        }
    }

    private void sendScanResult(int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        var data = Json.createObject();
        data.put("vulnerabilityCount", vulnerabilityCount);
        devToolsInterface.send("appsec-kit-scan", data);
    }
}
