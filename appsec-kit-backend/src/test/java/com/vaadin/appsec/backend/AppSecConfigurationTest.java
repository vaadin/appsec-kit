/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppSecConfigurationTest {

    @After
    public void clearProperties() {
        System.clearProperty(AppSecConfiguration.DATA_PATH_PROPERTY);
    }

    @Test
    public void dataPathNotSet_systemPropertyNotSet_defaultIsUsed() {
        AppSecConfiguration conf = new AppSecConfiguration();
        Path dataFilePath = conf.getDataFilePath();

        assertEquals(
                Paths.get(AppSecConfiguration.DEFAULT_DATA_FILE_PATH,
                        AppSecConfiguration.DEFAULT_DATA_FILE_NAME),
                dataFilePath);
    }

    @Test
    public void dataPathNotSet_systemPropertySet_systemPropertyIsUsed() {
        System.setProperty(AppSecConfiguration.DATA_PATH_PROPERTY,
                "custom-path");

        AppSecConfiguration conf = new AppSecConfiguration();
        Path dataFilePath = conf.getDataFilePath();

        assertEquals(
                Paths.get("custom-path",
                        AppSecConfiguration.DEFAULT_DATA_FILE_NAME),
                dataFilePath);
    }

    @Test
    public void dataPathSet_isUsed() {
        Path customPath = Paths.get("custom-file.json");

        AppSecConfiguration conf = new AppSecConfiguration();
        conf.setDataFilePath(customPath);

        assertEquals(customPath, conf.getDataFilePath());
    }
}
