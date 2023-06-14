/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration settings for AppSec Kit.
 */
public class AppSecConfiguration implements Serializable {

    static final String DATA_PATH_PROPERTY = "vaadin.appsec.data";

    /**
     * The default path of the file to store kit's data in the project root.
     */
    static final String DEFAULT_DATA_PATH = "";

    static final String FILE_NAME = "appsec-data.json";

    private Path dataFilePath;

    /**
     * Gets the data-file path.
     *
     * @return the data-file path, not {@code null}
     */
    public Path getDataFilePath() {
        if (dataFilePath == null) {
            String propertyPath = System.getProperty(DATA_PATH_PROPERTY,
                    DEFAULT_DATA_PATH);
            dataFilePath = Paths.get(propertyPath, FILE_NAME);
        }
        return dataFilePath;
    }

    /**
     * Sets the data-file path.
     *
     * @param dataFilePath
     *            the data-file path, not {@code null}
     */
    public void setDataFilePath(Path dataFilePath) {
        if (dataFilePath == null) {
            throw new IllegalArgumentException(
                    "The data-file path cannot be null");
        }
        this.dataFilePath = dataFilePath;
    }
}
