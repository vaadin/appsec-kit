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
import java.util.List;

import com.vaadin.appsec.backend.model.dto.Vulnerability;

/**
 * Event fired when a scan for vulnerabilities has been completed.
 */
public class AppSecScanEvent extends EventObject {

    AppSecScanEvent(AppSecService source) {
        super(source);
    }

    @Override
    public AppSecService getSource() {
        return (AppSecService) super.getSource();
    }

    /**
     * Gets the list of new vulnerabilities found on this scan. A vulnerability
     * is considered new if there is no developer assessment data for that
     * vulnerability.
     *
     * @return the list of new vulnerabilities
     */
    public List<Vulnerability> getNewVulnerabilities() {
        return getSource().getNewVulnerabilities();
    }
}
