/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container of informations about a dependency with a known vulnerability.
 */
public class Dependency implements Serializable {

    private String name;

    private List<String> affectedVersions = new ArrayList<>();

    /**
     * The name of the dependency.
     *
     * @return the name of the dependency
     */
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * A list of ranges corresponding to the dependency versions affected by the
     * same vulnerability.
     *
     * @return the affected version ranges
     */
    public List<String> getAffectedVersions() {
        return affectedVersions;
    }

    void setAffectedVersions(List<String> affectedVersions) {
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
        if (!(obj instanceof Dependency)) {
            return false;
        }
        Dependency other = (Dependency) obj;
        return Objects.equals(name, other.name);
    }
}
