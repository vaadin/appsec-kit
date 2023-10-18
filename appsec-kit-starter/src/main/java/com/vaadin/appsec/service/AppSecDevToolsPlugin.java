package com.vaadin.appsec.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;
import com.vaadin.flow.component.dependency.JsModule;

import elemental.json.Json;
import elemental.json.JsonObject;

@JsModule(value = "./appsec-kit/appsec-kit-plugin.ts", developmentOnly = true)
public class AppSecDevToolsPlugin implements DevToolsMessageHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecDevToolsPlugin.class);

    private boolean scanEventListenerAdded = false;

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        devToolsInterface.send("appsec-kit-init", Json.createObject());
        AppSecService appSecService = AppSecService.getInstance();

        if (!scanEventListenerAdded) {
            appSecService.addScanEventListener(scanEvent -> sendScanResult(
                    scanEvent.getNewVulnerabilities().size(),
                    devToolsInterface));
            scanEventListenerAdded = true;
            LOGGER.debug("Scan event listener added");
        }
        refreshScanResult(appSecService.getNewVulnerabilities().size(),
                devToolsInterface);
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Command received: " + command);
        devToolsInterface.send("command-received", data);
        return true;
    }

    private void sendScanResult(int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Scan completed");
        sendData("appsec-kit-scan", vulnerabilityCount, devToolsInterface);
        LOGGER.info(
                "Vulnerabilities sent to the client: " + vulnerabilityCount);
    }

    private void refreshScanResult(int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        sendData("appsec-kit-refresh", vulnerabilityCount, devToolsInterface);
    }

    private void sendData(String command, int vulnerabilityCount,
            DevToolsInterface devToolsInterface) {
        var data = Json.createObject();
        data.put("vulnerabilityCount", vulnerabilityCount);
        devToolsInterface.send(command, data);
    }
}
