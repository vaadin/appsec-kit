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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vaadin.appsec.backend.model.osv.response.Package;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "commit", "version", "package" })
public class QueryRequestPayload implements Serializable {

    @JsonProperty("commit")
    private String commit;
    @JsonProperty("version")
    private String version;
    @JsonProperty("package")
    private Package aPackage;

    /**
     * No args constructor for use in serialization.
     */
    public QueryRequestPayload() {
    }

    public QueryRequestPayload(String commit) {
        this(commit, null, null);
    }

    public QueryRequestPayload(String version, Package aPackage) {
        this(null, version, aPackage);
    }

    private QueryRequestPayload(String commit, String version,
            Package aPackage) {
        this.commit = commit;
        this.version = version;
        this.aPackage = aPackage;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Package getPackage() {
        return aPackage;
    }

    public void setPackage(Package aPackage) {
        this.aPackage = aPackage;
    }
}
