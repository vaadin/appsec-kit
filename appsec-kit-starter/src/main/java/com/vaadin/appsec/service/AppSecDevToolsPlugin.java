package com.vaadin.appsec.service;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<DevToolsInterface, Registration> scanEventRegistrations = new HashMap<>();

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        devToolsInterface.send("appsec-kit-init", Json.createObject());
        AppSecService appSecService = AppSecService.getInstance();

        if (!scanEventRegistrations.containsKey(devToolsInterface)) {
            Registration registration = appSecService
                    .addScanEventListener(event -> sendAndLogScanResult(
                            event.getNewVulnerabilities().size(),
                            devToolsInterface));
            scanEventRegistrations.put(devToolsInterface, registration);
            LOGGER.debug("Scan event listener added");
        }
        sendScanResult("appsec-kit-refresh",
                appSecService.getNewVulnerabilities().size(),
                devToolsInterface);
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Command received: " + command);
        devToolsInterface.send("command-received", data);
        return true;
    }

    private void sendAndLogScanResult(int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Scan completed");
        sendScanResult("appsec-kit-scan", vulnerabilityCount,
                devToolsInterface);
        LOGGER.info(
                "Vulnerabilities sent to the client: " + vulnerabilityCount);
    }

    private void sendScanResult(String command, int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        var data = Json.createObject();
        data.put("vulnerabilityCount", vulnerabilityCount);
        devToolsInterface.send(command, data);
    }
}
