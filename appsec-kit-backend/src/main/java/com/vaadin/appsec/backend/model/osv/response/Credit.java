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
 * DTO for credit informations returned by the OSV API.
 */
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

    /**
     * Instantiates a new credit.
     *
     * @param name
     *            the name
     * @param contact
     *            the contact
     * @param type
     *            the type
     */
    public Credit(String name, List<String> contact, Type type) {
        this.name = name;
        this.contact = contact;
        this.type = type;
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
     * Gets the contact.
     *
     * @return the contact
     */
    public List<String> getContact() {
        return contact;
    }

    /**
     * Sets the contact.
     *
     * @param contact
     *            the new contact
     */
    public void setContact(List<String> contact) {
        this.contact = contact;
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
     * The type of credit.
     */
    public enum Type {
        // @formatter:off
        FINDER("FINDER"),
        REPORTER("REPORTER"),
        ANALYST("ANALYST"),
        COORDINATOR("COORDINATOR"),
        REMEDIATION_DEVELOPER("REMEDIATION_DEVELOPER"),
        REMEDIATION_REVIEWER("REMEDIATION_REVIEWER"),
        REMEDIATION_VERIFIER("REMEDIATION_VERIFIER"),
        TOOL("TOOL"),
        SPONSOR("SPONSOR"),
        OTHER("OTHER");
        // @formatter:on
        private final String value;

        Type(String value) {
            this.value = value;
        }

        /**
         * The value as string.
         *
         * @return the value as string
         */
        public String value() {
            return this.value;
        }

        /**
         * Returns the instance from the string value.
         *
         * @param value
         *            the value as string
         * @return the type
         */
        public static Type fromValue(String value) {
            for (Type type : values()) {
                if (type.value().equalsIgnoreCase(value)) {
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
        return "Credit{" + "name='" + name + '\'' + ", contact=" + contact
                + ", type=" + type + '}';
    }
}
