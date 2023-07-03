/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The descriptor for a single module that contains assessments for each version
 * affected by the same vulnerability.
 */
public class Assessment implements Serializable {

    static class MapDeserializer extends JsonDeserializer<AffectedVersion> {

        @Override
        public AffectedVersion deserialize(JsonParser p,
                DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String range = ctxt.getParser().getCurrentName();
            AffectedVersion version = p.readValueAs(AffectedVersion.class);
            version.setVersionRange(range);
            return version;
        }
    }

    @JsonIgnore
    private String name;

    @JsonDeserialize(contentUsing = MapDeserializer.class)
    private Map<String, AffectedVersion> affectedVersions = new HashMap<>();

    /**
     * The name of the module.
     *
     * @return the name of the module
     */
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * A map of assessments on this module by version range.
     *
     * @return the map of assessments
     */
    public Map<String, AffectedVersion> getAffectedVersions() {
        return affectedVersions;
    }

    void setAffectedVersions(Map<String, AffectedVersion> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Assessment)) {
            return false;
        }
        Assessment other = (Assessment) obj;
        return Objects.equals(name, other.name);
    }
}
