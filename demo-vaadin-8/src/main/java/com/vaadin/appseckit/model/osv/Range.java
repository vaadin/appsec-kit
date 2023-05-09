package com.vaadin.appseckit.model.osv;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "repo",
        "events",
        "database_specific"
})
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

    public Range(Type type, String repo, List<Event> events,
                 DatabaseSpecific databaseSpecific) {
        this.type = type;
        this.repo = repo;
        this.events = events;
        this.databaseSpecific = databaseSpecific;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public DatabaseSpecific getDatabaseSpecific() {
        return databaseSpecific;
    }

    public void setDatabaseSpecific(DatabaseSpecific databaseSpecific) {
        this.databaseSpecific = databaseSpecific;
    }

    public enum Type {
        GIT("GIT"),
        SEMVER("SEMVER"),
        ECOSYSTEM("ECOSYSTEM");

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
