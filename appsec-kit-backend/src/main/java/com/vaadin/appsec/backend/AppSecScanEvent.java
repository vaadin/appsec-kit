/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.EventObject;

public class AppSecScanEvent extends EventObject {

    public AppSecScanEvent(AppSecService source) {
        super(source);
    }

    @Override
    public AppSecService getSource() {
        return (AppSecService) super.getSource();
    }
}
