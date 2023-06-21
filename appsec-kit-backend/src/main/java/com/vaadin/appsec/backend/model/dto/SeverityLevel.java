/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.dto;

/**
 * Severity level for a vulnerability
 */
public enum SeverityLevel {
    /**
     * High severity level.
     */
    HIGH("High"),
    /**
     * Medium severity level.
     */
    MEDIUM("Medium"),
    /**
     * Low severity level.
     */
    LOW("Low"),
    /**
     * Na severity level.
     */
    NA("---");

    private String caption;

    SeverityLevel(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
