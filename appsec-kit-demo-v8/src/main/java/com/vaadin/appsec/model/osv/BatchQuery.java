package com.vaadin.appsec.model.osv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchQuery {

    @JsonProperty("queries")
    private Query[] queries;

    /**
     * No args constructor for use in serialization.
     */
    public BatchQuery() {
    }

    public BatchQuery(Query[] queries) {
        this.queries = queries;
    }

    public Query[] getQueries() {
        return queries;
    }

    public void setQueries(Query[] queries) {
        this.queries = queries;
    }
}
