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

/**
 * The container for the payload of a query to the OSV API.
 */
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

    /**
     * Instantiates a new query request payload.
     *
     * @param commit
     *            the commit
     */
    public QueryRequestPayload(String commit) {
        this(commit, null, null);
    }

    /**
     * Instantiates a new query request payload.
     *
     * @param version
     *            the version
     * @param aPackage
     *            the a package
     */
    public QueryRequestPayload(String version, Package aPackage) {
        this(null, version, aPackage);
    }

    private QueryRequestPayload(String commit, String version,
            Package aPackage) {
        this.commit = commit;
        this.version = version;
        this.aPackage = aPackage;
    }

    /**
     * Gets the commit.
     *
     * @return the commit
     */
    public String getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit
     *            the new commit
     */
    public void setCommit(String commit) {
        this.commit = commit;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the package.
     *
     * @return the package
     */
    public Package getPackage() {
        return aPackage;
    }

    /**
     * Sets the package.
     *
     * @param aPackage
     *            the new package
     */
    public void setPackage(Package aPackage) {
        this.aPackage = aPackage;
    }

    @Override
    public String toString() {
        return "QueryRequestPayload{" + "commit='" + commit + '\''
                + ", version='" + version + '\'' + ", aPackage=" + aPackage
                + '}';
    }
}
