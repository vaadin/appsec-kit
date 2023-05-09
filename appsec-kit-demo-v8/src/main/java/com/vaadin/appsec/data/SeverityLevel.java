package com.vaadin.appsec.data;

public enum SeverityLevel {
    HIGH("High"), MEDIUM("Medium"), LOW("Low"), NA("---");

    private String caption;

    SeverityLevel(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
