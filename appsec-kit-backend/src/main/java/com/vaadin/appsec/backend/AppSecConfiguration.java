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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration settings for AppSec Kit.
 * <p>
 * An instance of this class can be set to configure {@link AppSecService} with
 * {@link AppSecService#setConfiguration(AppSecConfiguration).
 */
public class AppSecConfiguration implements Serializable {

    static final String DATA_PATH_PROPERTY = "vaadin.appsec.data";

    /**
     * The default path of the file to store kit's data in the project root.
     */
    static final String DEFAULT_DATA_FILE_PATH = "";

    static final String DEFAULT_DATA_FILE_NAME = "appsec-data.json";

    static final String DEFAULT_BOM_FILE_PATH = "/resources/bom.json";

    private Path dataFilePath;

    private Path bomFilePath;

    private Executor taskExecutor = Executors.newSingleThreadExecutor();

    /**
     * Gets the data-file path.
     *
     * @return the data-file path, not {@code null}
     */
    public Path getDataFilePath() {
        if (dataFilePath == null) {
            String propertyPath = System.getProperty(DATA_PATH_PROPERTY,
                    DEFAULT_DATA_FILE_PATH);
            dataFilePath = Paths.get(propertyPath, DEFAULT_DATA_FILE_NAME);
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

    /**
     * Gets the BOM-file path.
     *
     * @return the BOM-file path, not {@code null}
     */
    public Path getBomFilePath() {
        if (bomFilePath == null) {
            bomFilePath = Paths.get(DEFAULT_BOM_FILE_PATH);
        }
        return bomFilePath;
    }

    /**
     * Sets the BOM-file path.
     *
     * @param bomFilePath
     *            the BOM-file path, not {@code null}
     */
    public void setBomFilePath(Path bomFilePath) {
        if (bomFilePath == null) {
            throw new IllegalArgumentException(
                    "The BOM-file path cannot be null");
        }
        this.bomFilePath = bomFilePath;
    }

    /**
     * Gets the executor used to run asynchronous tasks.
     *
     * @return the task executor
     */
    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Sets the executor used to run asynchronous tasks.
     *
     * @param taskExecutor
     *            the task executor
     */
    public void setTaskExecutor(Executor taskExecutor) {
        if (taskExecutor == null) {
            throw new IllegalArgumentException("The executor cannot be null");
        }
        this.taskExecutor = taskExecutor;
    }
}
