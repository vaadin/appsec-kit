/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.Objects;

/**
 * Contains the supported Vaadin versions.
 */
public enum VaadinVersion {
    V7,
    V8,
    V24;

    public static boolean isFlow(VaadinVersion version) {
        return Objects.requireNonNull(version) == VaadinVersion.V24;
    }
}
