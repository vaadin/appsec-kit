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
import java.util.EventListener;

/**
 * A listener of {@link AppSecScanEvent} that can be added to
 * {@link AppSecService} with
 * {@link AppSecService#addScanEventListener(AppSecScanEventListener)}.
 */
@FunctionalInterface
public interface AppSecScanEventListener extends EventListener, Serializable {

    /**
     * Operation performed when a new scan event has completed.
     *
     * @param event
     *            the scan event
     */
    void scanCompleted(AppSecScanEvent event);
}
