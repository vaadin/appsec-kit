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
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration settings for AppSec Kit.
 * <p>
 * An instance of this class can be set to configure {@link AppSecService} with
 * {@link AppSecService#setConfiguration(AppSecConfiguration).
 */
public class AppSecConfiguration implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecConfiguration.class);

    static final String DATA_PATH_PROPERTY = "vaadin.appsec.data";

    /**
     * The default path of the file to store kit's data in the project root.
     */
    static final String DEFAULT_DATA_FILE_PATH = "";
    static final String DEFAULT_DATA_FILE_NAME = "appsec-data.json";
    static final String DEFAULT_BOM_FILE_PATH = "/resources";
    static final String BOM_MAVEN_PATH_PROPERTY = "vaadin.appsec.bom";
    static final String DEFAULT_BOM_MAVEN_FILE_NAME = "bom.json";
    static final String BOM_NPM_PATH_PROPERTY = "vaadin.appsec.bom-npm";
    static final String DEFAULT_BOM_NPM_FILE_NAME = "bom-npm.json";

    private Path dataFilePath;
    private Path bomMavenFilePath;
    private Path bomNpmFilePath;
    private ScheduledExecutorService taskExecutor = Executors
            .newSingleThreadScheduledExecutor();
    private Duration autoScanInterval = Duration.ofDays(1);
    private int osvApiRatePerSecond = 25;
    private boolean includeNpmDevDependencies = true;

    /**
     * Gets the data-file path.
     *
     * @return the data-file path, not {@code null}
     */
    public Path getDataFilePath() {
        if (dataFilePath == null) {
            String propertyPath = System.getProperty(DATA_PATH_PROPERTY,
                    DEFAULT_DATA_FILE_PATH);
            try {
                dataFilePath = Paths.get(propertyPath, DEFAULT_DATA_FILE_NAME);
            } catch (InvalidPathException e) {
                throw new AppSecException(
                        "Invalid data file path " + propertyPath, e);
            }
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
     * Gets the Maven BOM-file path.
     *
     * @return the Maven BOM-file path, not {@code null}
     */
    public Path getBomFilePath() {
        if (bomMavenFilePath == null) {
            String propertyPath = System.getProperty(BOM_MAVEN_PATH_PROPERTY,
                    DEFAULT_BOM_FILE_PATH);
            String bomFile = propertyPath + "/" + DEFAULT_BOM_MAVEN_FILE_NAME;
            URL bomFileUrl = AppSecConfiguration.class.getResource(bomFile);
            if (bomFileUrl != null) {
                try {
                    bomMavenFilePath = Paths.get(bomFileUrl.toURI());
                } catch (URISyntaxException e) {
                    throw new AppSecException(
                            "URI syntax error for Maven SBOM file path: "
                                    + bomFile,
                            e);
                } catch (InvalidPathException e) {
                    throw new AppSecException(
                            "Invalid Maven SBOM file path " + bomFile, e);
                } catch (FileSystemNotFoundException e) {
                    // Some web and application servers can use file systems
                    // which are not supported, therefore a relative path is
                    // returned
                    return Paths.get(bomFile);
                } catch (RuntimeException e) {
                    throw new AppSecException(
                            "Error occurred when getting the Maven SBOM file path",
                            e);
                }
            } else {
                throw new AppSecException(
                        "Maven SBOM file not found on path " + bomFile);
            }
        }
        return bomMavenFilePath;
    }

    /**
     * Sets the Maven BOM-file path.
     *
     * @param bomFilePath
     *            the Maven BOM-file path, not {@code null}
     */
    public void setBomFilePath(Path bomFilePath) {
        if (bomFilePath == null) {
            throw new IllegalArgumentException(
                    "The Maven BOM-file path cannot be null");
        }
        this.bomMavenFilePath = bomFilePath;
    }

    /**
     * Gets the npm BOM-file path.
     *
     * @return the npm BOM-file path, not {@code null}
     */
    public Path getBomNpmFilePath() {
        if (bomNpmFilePath == null) {
            String propertyPath = System.getProperty(BOM_NPM_PATH_PROPERTY,
                    DEFAULT_BOM_FILE_PATH);
            String bomNpmFile = propertyPath + "/" + DEFAULT_BOM_NPM_FILE_NAME;
            URL bomNpmFileUrl = AppSecConfiguration.class
                    .getResource(bomNpmFile);
            if (bomNpmFileUrl != null) {
                try {
                    bomNpmFilePath = Paths.get(bomNpmFileUrl.toURI());
                } catch (URISyntaxException e) {
                    throw new AppSecException(
                            "URI syntax error for npm SBOM file path: "
                                    + bomNpmFile,
                            e);
                } catch (InvalidPathException e) {
                    throw new AppSecException(
                            "Invalid npm SBOM file path " + bomNpmFile, e);
                } catch (FileSystemNotFoundException e) {
                    // Some web and application servers can use file systems
                    // which are not supported, therefore a relative path is
                    // returned
                    return Paths.get(bomNpmFile);
                } catch (RuntimeException e) {
                    throw new AppSecException(
                            "Error occurred when getting the npm SBOM file path",
                            e);
                }
            } else {
                LOGGER.warn("npm SBOM file not found on path " + bomNpmFile);
            }
        }
        return bomNpmFilePath;
    }

    /**
     * Sets the npm BOM-file path.
     *
     * @param bomNpmFilePath
     *            the npm BOM-file path, not {@code null}
     */
    public void setBomNpmFilePath(Path bomNpmFilePath) {
        if (bomNpmFilePath == null) {
            throw new IllegalArgumentException(
                    "The npm BOM-file path cannot be null");
        }
        this.bomNpmFilePath = bomNpmFilePath;
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

    /**
     * Gets the rate per second value for the OpenSourceVulnerability API calls.
     *
     * @return the rate per second value for the OpenSourceVulnerability API
     *         calls
     */
    public int getOsvApiRatePerSecond() {
        return osvApiRatePerSecond;
    }

    /**
     * Sets the rate per second value for the OpenSourceVulnerability API calls.
     *
     * @param osvApiRatePerSecond
     *            the rate per second value for the OpenSourceVulnerability API
     *            calls
     */
    public void setOsvApiRatePerSecond(int osvApiRatePerSecond) {
        this.osvApiRatePerSecond = osvApiRatePerSecond;
    }

    /**
     * Gets if the npm development dependencies should be included during
     * vulnerability scanning.
     *
     * @return true if the npm development dependencies should be included
     *         during vulnerability scanning, otherwise false
     */
    public boolean isIncludeNpmDevDependencies() {
        return includeNpmDevDependencies;
    }

    /**
     * Sets if the npm development dependencies should be included during
     * vulnerability scanning.
     *
     * @param includeNpmDevDependencies
     *            true if the npm development dependencies should be included
     *            during vulnerability scanning, otherwise false
     */
    public void setIncludeNpmDevDependencies(
            boolean includeNpmDevDependencies) {
        this.includeNpmDevDependencies = includeNpmDevDependencies;
    }

    @Override
    public String toString() {
        return "AppSecConfiguration{" + "dataFilePath=" + dataFilePath
                + ", bomFilePath=" + bomMavenFilePath
                + (bomNpmFilePath != null ? ", bomNpmFilePath=" + bomNpmFilePath
                        : "")
                + ", taskExecutor=" + taskExecutor + ", autoScanInterval="
                + autoScanInterval + ", osvApiRatePerSecond="
                + osvApiRatePerSecond + ", includeNpmDevDependencies="
                + includeNpmDevDependencies + '}';
    }
}
