/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.appsec.backend.AppSecConfiguration;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * The entry point of the Spring Boot application. Use the @PWA annotation make
 * the application installable on phones, tablets and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "appsec-kit-demo")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        AppSecConfiguration appSecConfiguration = new AppSecConfiguration();
        appSecConfiguration.setVaadinPlatformVersion("25.0.0-alpha12");
        AppSecService.getInstance().setConfiguration(appSecConfiguration);

        SpringApplication.run(Application.class, args);
    }
}
