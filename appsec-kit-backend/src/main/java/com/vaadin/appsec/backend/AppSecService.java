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

    private final AppSecConfiguration configuration;

    private AppSecData data;

    /**
     * Creates a new instance of the service.
     *
     * @param configuration
     *            the configuration bean
     */
    public AppSecService(AppSecConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the data object, reading it from the file-system if the file exists.
     *
     * @return the data object, not {@code null}
     */
    public AppSecData getData() {
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
    public void setData(AppSecData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "The data object cannot be null");
        }
        this.data = data;
        writeDataFile();
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
