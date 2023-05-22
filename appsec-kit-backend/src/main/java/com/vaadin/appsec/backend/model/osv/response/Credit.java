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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "contact", "type" })
public class Credit {

    @JsonProperty("name")
    private String name;
    @JsonProperty("contact")
    private List<String> contact;
    @JsonProperty("type")
    private Type type;

    /**
     * No args constructor for use in serialization.
     */
    public Credit() {
    }

    public Credit(String name, List<String> contact, Type type) {
        this.name = name;
        this.contact = contact;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getContact() {
        return contact;
    }

    public void setContact(List<String> contact) {
        this.contact = contact;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        FINDER("FINDER"), REPORTER("REPORTER"), ANALYST("ANALYST"), COORDINATOR(
                "COORDINATOR"), REMEDIATION_DEVELOPER(
                        "REMEDIATION_DEVELOPER"), REMEDIATION_REVIEWER(
                                "REMEDIATION_REVIEWER"), REMEDIATION_VERIFIER(
                                        "REMEDIATION_VERIFIER"), TOOL(
                                                "TOOL"), SPONSOR(
                                                        "SPONSOR"), OTHER(
                                                                "OTHER");

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
