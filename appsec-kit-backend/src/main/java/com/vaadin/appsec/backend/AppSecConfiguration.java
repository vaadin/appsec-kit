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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    private ScheduledExecutorService taskExecutor = Executors
            .newSingleThreadScheduledExecutor();

    private Duration autoScanInterval = Duration.ofDays(1);

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
            try {
                bomFilePath = Paths.get(AppSecConfiguration.class
                        .getResource(DEFAULT_BOM_FILE_PATH).toURI());
            } catch (URISyntaxException e) {
                throw new AppSecException(
                        "Invalid SBOM file path: " + DEFAULT_BOM_FILE_PATH, e);
            }
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
    public ScheduledExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Sets the executor used to run asynchronous tasks.
     *
     * @param taskExecutor
     *            the task executor
     */
    public void setTaskExecutor(ScheduledExecutorService taskExecutor) {
        if (taskExecutor == null) {
            throw new IllegalArgumentException("The executor cannot be null");
        }
        this.taskExecutor = taskExecutor;
    }

    /**
     * Gets the duration of the interval between automatic scanning for
     * vulnerabilities.
     *
     * @return the duration of the interval between automatic scanning
     */
    public Duration getAutoScanInterval() {
        return autoScanInterval;
    }

    /**
     * Sets the duration of the interval between automatic scanning for
     * vulnerabilities. The default interval is 1 day.
     * <p>
     * A custom interval can be created using
     * {@link Duration#of(long, java.time.temporal.TemporalUnit)}.
     *
     * @param autoScanInterval
     *            the duration of the interval between automatic scanning
     */
    public void setAutoScanInterval(Duration autoScanInterval) {
        if (autoScanInterval == null) {
            throw new IllegalArgumentException(
                    "The auto-scan period cannot be null");
        }
        this.autoScanInterval = autoScanInterval;
    }

    @Override
    public String toString() {
        return "AppSecConfiguration{" + "dataFilePath=" + dataFilePath
                + ", bomFilePath=" + bomFilePath + ", taskExecutor="
                + taskExecutor + ", autoScanInterval=" + autoScanInterval + '}';
    }
}
