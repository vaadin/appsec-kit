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

        assertEquals(AppSecConfiguration.DEFAULT_DATA_PATH,
                dataFilePath.toString());
    }

    @Test
    public void dataPathNotSet_systemPropertySet_systemPropertyIsUsed() {
        System.setProperty(AppSecConfiguration.DATA_PATH_PROPERTY,
                "custom-path.json");

        AppSecConfiguration conf = new AppSecConfiguration();
        Path dataFilePath = conf.getDataFilePath();

        assertEquals("custom-path.json", dataFilePath.toString());
    }

    @Test
    public void dataPathSet_isUsed() {
        Path customPath = Path.of("custom-path.json");

        AppSecConfiguration conf = new AppSecConfiguration();
        conf.setDataFilePath(customPath);

        assertEquals(customPath, conf.getDataFilePath());
    }
}
