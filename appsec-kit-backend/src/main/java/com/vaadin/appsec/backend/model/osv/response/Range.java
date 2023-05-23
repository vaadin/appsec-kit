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
 * DTO for the OSV API range property.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "repo", "events", "database_specific" })
public class Range {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("repo")
    private String repo;
    @JsonProperty("events")
    private List<Event> events;
    @JsonProperty("database_specific")
    private DatabaseSpecific databaseSpecific;

    /**
     * No args constructor for use in serialization.
     */
    public Range() {
    }

    /**
     * Instantiates a new range.
     *
     * @param type
     *            the type
     * @param repo
     *            the repo
     * @param events
     *            the events
     * @param databaseSpecific
     *            the database specific
     */
    public Range(Type type, String repo, List<Event> events,
            DatabaseSpecific databaseSpecific) {
        this.type = type;
        this.repo = repo;
        this.events = events;
        this.databaseSpecific = databaseSpecific;
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
     * Gets the repo.
     *
     * @return the repo
     */
    public String getRepo() {
        return repo;
    }

    /**
     * Sets the repo.
     *
     * @param repo
     *            the new repo
     */
    public void setRepo(String repo) {
        this.repo = repo;
    }

    /**
     * Gets the events.
     *
     * @return the events
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Sets the events.
     *
     * @param events
     *            the new events
     */
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * Gets the database specific.
     *
     * @return the database specific
     */
    public DatabaseSpecific getDatabaseSpecific() {
        return databaseSpecific;
    }

    /**
     * Sets the database specific.
     *
     * @param databaseSpecific
     *            the new database specific
     */
    public void setDatabaseSpecific(DatabaseSpecific databaseSpecific) {
        this.databaseSpecific = databaseSpecific;
    }

    /**
     * The range type.
     */
    public enum Type {
        GIT("GIT"), SEMVER("SEMVER"), ECOSYSTEM("ECOSYSTEM");

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
         * Returns the type instance from the string value.
         *
         * @param value
         *            the string value
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
}
