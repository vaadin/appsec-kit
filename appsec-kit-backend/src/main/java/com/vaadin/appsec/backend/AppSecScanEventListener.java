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

@FunctionalInterface
public interface AppSecScanEventListener extends EventListener, Serializable {

    public void scanCompleted(AppSecScanEvent event);
}
