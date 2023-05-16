package com.vaadin.appsec.backend.model.osv.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ecosystem", "name", "purl" })
public class Package implements Serializable {

    @JsonProperty("ecosystem")
    private String ecosystem;
    @JsonProperty("name")
    private String name;
    @JsonProperty("purl")
    private String purl;

    /**
     * No args constructor for use in serialization.
     */
    public Package() {
    }

    /**
     * Constructs {@link Package} with ecosystem and name.
     *
     * @param ecosystem
     *            The ecosystem for this package. For the complete list of valid
     *            ecosystem names, see <a href=
     *            "https://ossf.github.io/osv-schema/#affectedpackage-field">ecosystems
     *            list</a>
     * @param name
     *            Name of the package
     */
    public Package(String ecosystem, String name) {
        this(ecosystem, name, null);
    }

    /**
     * Constructs {@link Package} with package URL.
     *
     * @param purl
     *            Package URL. Format
     *            <b>scheme:type/namespace/name@version?qualifiers#subpath<b/>,
     *            see <a href=
     *            "https://github.com/package-url/purl-spec#purl">Package URL
     *            specification</a>
     */
    public Package(String purl) {
        this(null, null, purl);
    }

    private Package(String ecosystem, String name, String purl) {
        this.ecosystem = ecosystem;
        this.name = name;
        this.purl = purl;
    }

    public String getEcosystem() {
        return ecosystem;
    }

    public void setEcosystem(String ecosystem) {
        this.ecosystem = ecosystem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }
}
