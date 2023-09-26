package com.vaadin.appsec.v24.service;

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

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        LOGGER.info("Plugin connected");
        devToolsInterface.send("appsec-kit-init", Json.createObject());
        AppSecService.getInstance().addScanEventListener(scanEvent -> {
            LOGGER.info("Scan completed");
            var vulnerabilityCount = scanEvent.getNewVulnerabilities().size();
            var data = Json.createObject();
            data.put("vulnerabilityCount", vulnerabilityCount);
            devToolsInterface.send("appsec-kit-scan", data);
            LOGGER.info("Vulnerabilities sent to the client: "
                    + vulnerabilityCount);
        });
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        LOGGER.info("Command received: " + command);
        devToolsInterface.send("command-received", data);
        return true;
    }
}
