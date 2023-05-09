package com.vaadin.appseckit.model.osv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "commit",
        "version",
        "package"
})
public class Query {

    @JsonProperty("commit")
    private String commit;
    @JsonProperty("version")
    private String version;
    @JsonProperty("package")
    private Package aPackage;

    /**
     * No args constructor for use in serialization.
     */
    public Query() {
    }

    public Query(String commit) {
        this(commit, null, null);
    }

    public Query(String version, Package aPackage) {
        this(null, version, aPackage);
    }

    private Query(String commit, String version, Package aPackage) {
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
