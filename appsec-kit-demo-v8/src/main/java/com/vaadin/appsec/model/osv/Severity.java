package com.vaadin.appsec.model.osv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "score"
})
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

    public Severity(Type type, String score) {
        this.type = type;
        this.score = score;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public enum Type {
        CVSS_V2("CVSS_V2"),
        CVSS_V3("CVSS_V3");

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
