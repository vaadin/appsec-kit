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

/**
 * A registration object for removing an event listener added to a source.
 */
@FunctionalInterface
public interface Registration extends Serializable {

    /**
     * Removes the associated listener from the event source.
     */
    void remove();
}
