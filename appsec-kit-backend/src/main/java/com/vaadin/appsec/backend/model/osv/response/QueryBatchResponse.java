package com.vaadin.appsec.backend.model.osv.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryBatchResponse {

    @JsonProperty("results")
    private VulnerabilityIdArray[] results;

    /**
     * No args constructor for use in serialization.
     */
    public QueryBatchResponse() {
    }

    public QueryBatchResponse(VulnerabilityIdArray[] results) {
        this.results = results;
    }

    public VulnerabilityIdArray[] getResults() {
        return results;
    }

    public void setResults(VulnerabilityIdArray[] results) {
        this.results = results;
    }
}
