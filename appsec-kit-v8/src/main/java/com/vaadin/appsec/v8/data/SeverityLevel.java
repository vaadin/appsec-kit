/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.data;

public enum SeverityLevel {
    HIGH("High"), MEDIUM("Medium"), LOW("Low"), NA("---");

    private String caption;

    SeverityLevel(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
