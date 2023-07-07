/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.request;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The container for the payload of a batch query to the OSV API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryBatchRequestPayload implements Serializable {

    @JsonProperty("queries")
    private QueryRequestPayload[] queries;

    /**
     * No args constructor for use in serialization.
     */
    public QueryBatchRequestPayload() {
    }

    /**
     * Instantiates a new query batch request payload.
     *
     * @param queries
     *            the queries
     */
    public QueryBatchRequestPayload(QueryRequestPayload[] queries) {
        this.queries = queries;
    }

    /**
     * Gets the queries.
     *
     * @return the queries
     */
    public QueryRequestPayload[] getQueries() {
        return queries;
    }

    /**
     * Sets the queries.
     *
     * @param queries
     *            the new queries
     */
    public void setQueries(QueryRequestPayload[] queries) {
        this.queries = queries;
    }

    @Override
    public String toString() {
        return "QueryBatchRequestPayload{" + "queries="
                + Arrays.toString(queries) + '}';
    }
}
