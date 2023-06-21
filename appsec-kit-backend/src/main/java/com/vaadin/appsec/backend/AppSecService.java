/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.vaadin.appsec.backend.model.AppSecData;

/**
 * Service that provides access to all AppSec Kit features, such as
 * vulnerability scanning and analysis storage.
 */
public class AppSecService {

    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private static final class InstanceHolder {
        static final AppSecService instance = new AppSecService(
                new AppSecConfiguration());
    }

    /**
     * Get the AppSecService singleton instance.
     *
     * @return singleton the singleton instance
     */
    public static AppSecService getInstance() {
        return AppSecService.InstanceHolder.instance;
    }

    private AppSecConfiguration configuration;

    private AppSecData data;

    /**
     * Creates a new instance of the service.
     *
     * @param configuration
     *            the configuration bean
     */
    private AppSecService(AppSecConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the data object, reading it from the file-system if the file exists.
     *
     * @return the data object, not {@code null}
     */
    public synchronized AppSecData getData() {
        if (data == null) {
            data = readOrCreateDataFile();
        }
        return data;
    }

    /**
     * Sets the data object, writing it to the file-system.
     *
     * @param data
     *            the data object, not {@code null}
     */
    public synchronized void setData(AppSecData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "The data object cannot be null");
        }
        this.data = data;
        writeDataFile();
    }

    /**
     * Re-reads data from disk.
     *
     * @return the data object, not {@code null}
     */
    public synchronized AppSecData refresh() {
        data = null;
        return getData();
    }

    /**
     * Updates the last scanned timestamp to current time and writes the data to
     * disk.
     */
    public synchronized void updateLastScanTime() {
        AppSecData tempData = getData();
        tempData.setLastScan(Instant.now());
        setData(tempData);
    }

    /**
     * Allows to set the configuration for this singleton instance.
     *
     * @param configuration
     *            configuration to set
     */
    public synchronized void setConfiguration(
            AppSecConfiguration configuration) {
        this.configuration = configuration;
        this.data = null;
    }

    private AppSecData readOrCreateDataFile() {
        File dataFile = configuration.getDataFilePath().toFile();
        if (dataFile.exists()) {
            try {
                return MAPPER.readValue(dataFile, AppSecData.class);
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot read the AppSec Kit data file: "
                                + configuration.getDataFilePath().toString(),
                        e);
            }
        } else {
            return new AppSecData();
        }
    }

    private void writeDataFile() {
        if (data != null) {
            File dataFile = configuration.getDataFilePath().toFile();
            try {
                MAPPER.writeValue(dataFile, data);
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot write the AppSec Kit data file: "
                                + configuration.getDataFilePath().toString(),
                        e);
            }
        }
    }
}
