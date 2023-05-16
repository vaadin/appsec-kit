package com.vaadin.appsec.backend.model.osv.response;

public enum Ecosystem {
    MAVEN("Maven"),
    NPM("npm");

    private final String value;

    Ecosystem(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Ecosystem fromValue(String value) {
        for (Ecosystem ecosystem : values()) {
            if (ecosystem.value().equals(value)) {
                return ecosystem;
            }
        }
        throw new IllegalArgumentException(value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
