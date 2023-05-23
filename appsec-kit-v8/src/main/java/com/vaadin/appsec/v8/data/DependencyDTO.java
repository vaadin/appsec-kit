/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.data;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DTO for a dependency instance, used in the UI.
 */
public class DependencyDTO {

    private String group;
    private String name;
    private String version;

    private Integer numOfVulnerabilities;

    private SeverityLevel severityLevel;
    private Double riskScore;

    public DependencyDTO(@NotNull String group, @NotNull String name,
            @NotNull String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DependencyDTO that = (DependencyDTO) o;
        return Objects.equals(group, that.group)
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }

    public Integer getNumOfVulnerabilities() {
        return numOfVulnerabilities;
    }

    public void setNumOfVulnerabilities(Integer numOfVulnerabilities) {
        this.numOfVulnerabilities = numOfVulnerabilities;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    @Override
    public String toString() {
        return group + ":" + name;
    }
}
