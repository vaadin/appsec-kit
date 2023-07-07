/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.response;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DTO for the OSV API reference property.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "url" })
public class Reference {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("url")
    private URI url;

    /**
     * No args constructor for use in serialization.
     */
    public Reference() {
    }

    /**
     * Instantiates a new reference.
     *
     * @param type
     *            the type
     * @param url
     *            the url
     */
    public Reference(Type type, URI url) {
        this.type = type;
        this.url = url;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public URI getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url
     *            the new url
     */
    public void setUrl(URI url) {
        this.url = url;
    }

    /**
     * The reference type.
     */
    public enum Type {
        // @formatter:off
        ADVISORY("ADVISORY"),
        ARTICLE("ARTICLE"),
        DETECTION("DETECTION"),
        DISCUSSION("DISCUSSION"),
        REPORT("REPORT"),
        FIX("FIX"),
        INTRODUCED("INTRODUCED"),
        GIT("GIT"),
        PACKAGE("PACKAGE"),
        EVIDENCE("EVIDENCE"),
        WEB("WEB");
        // @formatter:on
        private final String value;

        Type(String value) {
            this.value = value;
        }

        /**
         * Returns the value as string.
         *
         * @return the value as string
         */
        public String value() {
            return this.value;
        }

        /**
         * Returns the type from a string value.
         *
         * @param value
         *            the value as string
         * @return the type
         */
        public static Type fromValue(String value) {
            for (Type type : values()) {
                if (type.value().equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException(value);
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    @Override
    public String toString() {
        return "Reference{" + "type=" + type + ", url=" + url + '}';
    }
}
