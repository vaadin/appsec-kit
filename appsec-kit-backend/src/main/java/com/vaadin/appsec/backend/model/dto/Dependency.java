/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.dto;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import com.vaadin.appsec.backend.model.osv.response.Ecosystem;

/**
 * DTO for a dependency instance, used in the UI.
 */
public class Dependency {

    private Ecosystem ecosystem;
    private String group;
    private String name;
    private String version;
    private String parentBomRef;
    private Integer numOfVulnerabilities;
    private SeverityLevel severityLevel;
    private Double riskScore;
    private String cvssString;
    private boolean devDependency = false;

    /**
     * Instantiates a new Dependency dto.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     * @param version
     *            the version
     */
    public Dependency(@NotNull Ecosystem ecosystem, String group,
            @NotNull String name, @NotNull String version) {
        this.ecosystem = ecosystem;
        this.group = group;
        this.name = name;
        this.version = version;
    }

    /**
     * Gets ecosystem.
     *
     * @return the ecosystem
     */
    public Ecosystem getEcosystem() {
        return ecosystem;
    }

    /**
     * Sets ecosystem.
     *
     * @param ecosystem
     *            the ecosystem
     */
    public void setEcosystem(Ecosystem ecosystem) {
        this.ecosystem = ecosystem;
    }

    /**
     * Gets group.
     *
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets group.
     *
     * @param group
     *            the group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param version
     *            the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets parent dependency's BOM reference (purl).
     *
     * @return the parent dependency's BOM reference (purl)
     */
    public String getParentBomRef() {
        return parentBomRef;
    }

    /**
     * Sets parent dependency's BOM reference (purl).
     *
     * @param parentBomRef
     *            the parent dependency's BOM reference (purl)
     */
    public void setParentBomRef(String parentBomRef) {
        this.parentBomRef = parentBomRef;
    }

    /**
     * Gets num of vulnerabilities.
     *
     * @return the num of vulnerabilities
     */
    public Integer getNumOfVulnerabilities() {
        return numOfVulnerabilities;
    }

    /**
     * Sets num of vulnerabilities.
     *
     * @param numOfVulnerabilities
     *            the num of vulnerabilities
     */
    public void setNumOfVulnerabilities(Integer numOfVulnerabilities) {
        this.numOfVulnerabilities = numOfVulnerabilities;
    }

    /**
     * Gets severity level.
     *
     * @return the severity level
     */
    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    /**
     * Sets severity level.
     *
     * @param severityLevel
     *            the severity level
     */
    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

    /**
     * Gets risk score.
     *
     * @return the risk score
     */
    public Double getRiskScore() {
        return riskScore;
    }

    /**
     * Sets risk score.
     *
     * @param riskScore
     *            the risk score
     */
    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    /**
     * Gets CVSS string.
     *
     * @return the CVSS string
     */
    public String getCvssString() {
        return cvssString;
    }

    /**
     * Sets CVSS string.
     *
     * @param cvssString
     *            the CVSS string
     */
    public void setCvssString(String cvssString) {
        this.cvssString = cvssString;
    }

    /**
     * Shows if a npm dependency is a dev dependency. For the Maven dependencies
     * this is always false.
     *
     * @return true if the npm dependency is a dev dependency, otherwise false
     */
    public boolean isDevDependency() {
        return devDependency;
    }

    /**
     * Sets the npm dev dependency. For the Maven dependencies this should be
     * set to false.
     *
     * @param devDependency
     *            the npm dev dependency
     */
    public void setDevDependency(boolean devDependency) {
        this.devDependency = devDependency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Dependency that = (Dependency) o;
        return Objects.equals(group, that.group)
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }

    @Override
    public String toString() {
        return group + ":" + name;
    }
}
