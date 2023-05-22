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
