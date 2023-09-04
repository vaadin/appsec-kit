/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.osv.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DTO for the OSV API severity property.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "score" })
public class Severity {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("score")
    private String score;

    /**
     * No args constructor for use in serialization.
     */
    public Severity() {
    }

    /**
     * Instantiates a new severity.
     *
     * @param type
     *            the type
     * @param score
     *            the score
     */
    public Severity(Type type, String score) {
        this.type = type;
        this.score = score;
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
     * Gets the score.
     *
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * Sets the score.
     *
     * @param score
     *            the new score
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * The severity type.
     */
    public enum Type {
        CVSS_V2("CVSS_V2"), CVSS_V3("CVSS_V3");

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
        return "Severity{" + "type=" + type + ", score='" + score + '\'' + '}';
    }
}
