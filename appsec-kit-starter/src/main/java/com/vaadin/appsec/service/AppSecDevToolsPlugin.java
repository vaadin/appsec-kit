/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import elemental.json.JsonObject;

@JsModule(value = "./appsec-kit/appsec-kit-plugin.ts", developmentOnly = true)
public class AppSecDevToolsPlugin implements DevToolsMessageHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecDevToolsPlugin.class);

    private static final String APPSEC_KIT_INIT = "appsec-kit-init";
    private static final String APPSEC_KIT_UNLOAD = "appsec-kit-unload";

    private final Map<String, Map<UI, Registration>> sessionScanEventRegistrations = new HashMap<>();

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        devToolsInterface.send(APPSEC_KIT_INIT, Json.createObject());
        handleRegistrations(APPSEC_KIT_INIT, devToolsInterface);
        sendScanResult(
                AppSecService.getInstance().getNewVulnerabilities().size(),
                devToolsInterface);
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        LOGGER.debug("Command received: " + command);
        if (command.equals(APPSEC_KIT_UNLOAD)) {
            handleRegistrations(command, devToolsInterface);
            return true;
        } else {
            return false; // Not a plugin command
        }
    }

    private void handleRegistrations(String command,
            DevToolsInterface devToolsInterface) {
        var session = VaadinSession.getCurrent();
        session.access(() -> {
            var sessionId = session.getSession().getId();
            var uis = new ArrayList<>(session.getUIs());
            var uiScanEventRegistrations = sessionScanEventRegistrations
                    .computeIfAbsent(sessionId, s -> new HashMap<>());

            // Adds the scan event listeners and stores the registrations
            uis.forEach(ui -> {
                if (!uiScanEventRegistrations.containsKey(ui)) {
                    var appSecService = AppSecService.getInstance();
                    var registration = appSecService
                            .addScanEventListener(event -> {
                                var count = event.getNewVulnerabilities()
                                        .size();
                                sendScanResult(count, devToolsInterface);
                                LOGGER.debug(
                                        "Scan event received. Vulnerabilities sent to the client: "
                                                + count);
                            });
                    uiScanEventRegistrations.put(ui, registration);
                }
            });

            // Removes scan event listeners and registrations
            var registrationsToRemove = uiScanEventRegistrations.keySet()
                    .stream().filter(ui -> !uis.contains(ui))
                    .collect(Collectors.toList());
            if (command.equals(APPSEC_KIT_UNLOAD) && uis.size() == 1) {
                // In case of page unload (page refresh, closing a tab or a
                // window) if the session contains only one UI (the currently
                // unloading one) then we remove that UI and the session from
                // the registrations because in case of closing a tab or a
                // window these will be removed, and we don't want to store them
                // anymore. In case of page refresh a new UI will be created and
                // stored in a newly added session registrations.
                registrationsToRemove.add(uis.get(0));
            }
            registrationsToRemove.forEach(ui -> {
                var registration = uiScanEventRegistrations.get(ui);
                registration.remove();
                uiScanEventRegistrations.remove(ui);
                if (uiScanEventRegistrations.isEmpty()) {
                    sessionScanEventRegistrations.remove(sessionId);
                }
            });
        });
    }

    private void sendScanResult(int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        var data = Json.createObject();
        data.put("vulnerabilityCount", vulnerabilityCount);
        devToolsInterface.send("appsec-kit-scan", data);
    }
}
