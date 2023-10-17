package com.vaadin.appsec.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;
import com.vaadin.flow.component.dependency.JsModule;

import elemental.json.Json;
import elemental.json.JsonObject;

@JsModule(value = "./appsec-kit/appsec-kit-plugin.ts", developmentOnly = true)
public class AppSecDevToolsPlugin implements DevToolsMessageHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecDevToolsPlugin.class);

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        LOGGER.debug("Plugin connected");
        devToolsInterface.send("appsec-kit-init", Json.createObject());
        AppSecService appSecService = AppSecService.getInstance();

        appSecService.addScanEventListener(scanEvent -> sendScanResult(
                scanEvent.getNewVulnerabilities(), devToolsInterface));
        LOGGER.debug("Scan event listener added");

        if (appSecService.getData().getLastScan() == null) {
            appSecService.scanForVulnerabilities();
            sendScanResult(appSecService.getNewVulnerabilities(),
                    devToolsInterface);
        }
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Command received: " + command);
        devToolsInterface.send("command-received", data);
        return true;
    }

    private void sendScanResult(List<Vulnerability> vulnerabilities,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Scan completed");
        var vulnerabilityCount = vulnerabilities.size();
        var data = Json.createObject();
        data.put("vulnerabilityCount", vulnerabilityCount);
        devToolsInterface.send("appsec-kit-scan", data);
        LOGGER.info(
                "Vulnerabilities sent to the client: " + vulnerabilityCount);
    }
}
