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

    public Reference(Type type, URI url) {
        this.type = type;
        this.url = url;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public enum Type {
        ADVISORY("ADVISORY"), ARTICLE("ARTICLE"), DETECTION(
                "DETECTION"), DISCUSSION("DISCUSSION"), REPORT("REPORT"), FIX(
                        "FIX"), INTRODUCED("INTRODUCED"), GIT("GIT"), PACKAGE(
                                "PACKAGE"), EVIDENCE("EVIDENCE"), WEB("WEB");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

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
}
