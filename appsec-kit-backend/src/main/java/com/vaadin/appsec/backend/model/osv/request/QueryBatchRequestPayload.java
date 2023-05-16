package com.vaadin.appsec.backend.model.osv.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryBatchRequestPayload implements Serializable {

    @JsonProperty("queries")
    private QueryRequestPayload[] queries;

    /**
     * No args constructor for use in serialization.
     */
    public QueryBatchRequestPayload() {
    }

    public QueryBatchRequestPayload(QueryRequestPayload[] queries) {
        this.queries = queries;
    }

    public QueryRequestPayload[] getQueries() {
        return queries;
    }

    public void setQueries(QueryRequestPayload[] queries) {
        this.queries = queries;
    }
}
