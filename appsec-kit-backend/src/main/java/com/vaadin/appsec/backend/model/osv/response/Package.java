/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DTO for the OSV API package property.
 */
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

    /**
     * Gets the ecosystem.
     *
     * @return the ecosystem
     */
    public String getEcosystem() {
        return ecosystem;
    }

    /**
     * Sets the ecosystem.
     *
     * @param ecosystem
     *            the new ecosystem
     */
    public void setEcosystem(String ecosystem) {
        this.ecosystem = ecosystem;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the purl.
     *
     * @return the purl
     */
    public String getPurl() {
        return purl;
    }

    /**
     * Sets the purl.
     *
     * @param purl
     *            the new purl
     */
    public void setPurl(String purl) {
        this.purl = purl;
    }

    @Override
    public String toString() {
        return "Package{" + "ecosystem='" + ecosystem + '\'' + ", name='" + name
                + '\'' + ", purl='" + purl + '\'' + '}';
    }
}
