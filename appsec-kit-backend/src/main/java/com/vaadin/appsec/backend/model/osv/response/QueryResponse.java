package com.vaadin.appsec.backend.model.osv.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResponse {

    @JsonProperty("vulns")
    private OpenSourceVulnerability[] vulnerabilities;

    /**
     * No args constructor for use in serialization.
     */
    public QueryResponse() {
    }

    public QueryResponse(OpenSourceVulnerability[] vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public OpenSourceVulnerability[] getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(OpenSourceVulnerability[] vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
