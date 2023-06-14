/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

/**
 * Exception that might occur during AppSec Kit operations.
 */
public class AppSecException extends RuntimeException {

    AppSecException(String message, Throwable cause) {
        super(message, cause);
    }
}
