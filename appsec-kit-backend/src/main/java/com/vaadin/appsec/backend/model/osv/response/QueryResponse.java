/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.response;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the OSV API query response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResponse {

    @JsonProperty("vulns")
    private OpenSourceVulnerability[] vulnerabilities;

    /**
     * No args constructor for use in serialization.
     */
    public QueryResponse() {
    }

    /**
     * Instantiates a new query response.
     *
     * @param vulnerabilities
     *            the vulnerabilities
     */
    public QueryResponse(OpenSourceVulnerability[] vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    /**
     * Gets the vulnerabilities.
     *
     * @return the vulnerabilities
     */
    public OpenSourceVulnerability[] getVulnerabilities() {
        return vulnerabilities;
    }

    /**
     * Sets the vulnerabilities.
     *
     * @param vulnerabilities
     *            the new vulnerabilities
     */
    public void setVulnerabilities(OpenSourceVulnerability[] vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    @Override
    public String toString() {
        return "QueryResponse{" + "vulnerabilities="
                + Arrays.toString(vulnerabilities) + '}';
    }
}
