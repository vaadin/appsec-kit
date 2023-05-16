package com.vaadin.appsec.backend.model.osv.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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

    public Package getPackage() {
        return aPackage;
    }

    public void setPackage(Package aPackage) {
        this.aPackage = aPackage;
    }

    public List<Severity> getSeverity() {
        return severity;
    }

    public void setSeverity(List<Severity> severity) {
        this.severity = severity;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public EcosystemSpecific getEcosystemSpecific() {
        return ecosystemSpecific;
    }

    public void setEcosystemSpecific(EcosystemSpecific ecosystemSpecific) {
        this.ecosystemSpecific = ecosystemSpecific;
    }

    public DatabaseSpecific getDatabaseSpecific() {
        return databaseSpecific;
    }

    public void setDatabaseSpecific(DatabaseSpecific databaseSpecific) {
        this.databaseSpecific = databaseSpecific;
    }
}
