/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DTO for information on dependencies affected by vulnerabilities.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "package", "severity", "ranges", "versions",
        "ecosystem_specific", "database_specific" })
public class Affected {

    @JsonProperty("package")
    private Package aPackage;
    @JsonProperty("severity")
    private List<Severity> severity;
    @JsonProperty("ranges")
    private List<Range> ranges;
    @JsonProperty("versions")
    private List<String> versions;
    @JsonProperty("ecosystem_specific")
    private EcosystemSpecific ecosystemSpecific;
    @JsonProperty("database_specific")
    private DatabaseSpecific databaseSpecific;

    /**
     * No args constructor for use in serialization.
     */
    public Affected() {
    }

    /**
     * Creates a new instance.
     *
     * @param aPackage
     *            the a package
     * @param severity
     *            the severity
     * @param ranges
     *            the ranges
     * @param versions
     *            the versions
     * @param ecosystemSpecific
     *            the ecosystem specific
     * @param databaseSpecific
     *            the database specific
     */
    public Affected(Package aPackage, List<Severity> severity,
            List<Range> ranges, List<String> versions,
            EcosystemSpecific ecosystemSpecific,
            DatabaseSpecific databaseSpecific) {
        this.aPackage = aPackage;
        this.severity = severity;
        this.ranges = ranges;
        this.versions = versions;
        this.ecosystemSpecific = ecosystemSpecific;
        this.databaseSpecific = databaseSpecific;
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

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public List<Severity> getSeverity() {
        return severity;
    }

    /**
     * Sets the severity.
     *
     * @param severity
     *            the new severity
     */
    public void setSeverity(List<Severity> severity) {
        this.severity = severity;
    }

    /**
     * Gets the ranges.
     *
     * @return the ranges
     */
    public List<Range> getRanges() {
        return ranges;
    }

    /**
     * Sets the ranges.
     *
     * @param ranges
     *            the new ranges
     */
    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

    /**
     * Gets the versions.
     *
     * @return the versions
     */
    public List<String> getVersions() {
        return versions;
    }

    /**
     * Sets the versions.
     *
     * @param versions
     *            the new versions
     */
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    /**
     * Gets the ecosystem specific.
     *
     * @return the ecosystem specific
     */
    public EcosystemSpecific getEcosystemSpecific() {
        return ecosystemSpecific;
    }

    /**
     * Sets the ecosystem specific.
     *
     * @param ecosystemSpecific
     *            the new ecosystem specific
     */
    public void setEcosystemSpecific(EcosystemSpecific ecosystemSpecific) {
        this.ecosystemSpecific = ecosystemSpecific;
    }

    /**
     * Gets the database specific.
     *
     * @return the database specific
     */
    public DatabaseSpecific getDatabaseSpecific() {
        return databaseSpecific;
    }

    /**
     * Sets the database specific.
     *
     * @param databaseSpecific
     *            the new database specific
     */
    public void setDatabaseSpecific(DatabaseSpecific databaseSpecific) {
        this.databaseSpecific = databaseSpecific;
    }

    @Override
    public String toString() {
        return "Affected{" + "aPackage=" + aPackage + ", ranges=" + ranges
                + '}';
    }
}
