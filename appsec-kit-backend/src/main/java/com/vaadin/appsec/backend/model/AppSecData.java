/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object to store information about vulnerabilities and be (de)serialized
 * into a JSON file committed to version control.
 */
public class AppSecData implements Serializable {

    /**
     * The status of the analysis for a single vulnerability.
     */
    public enum VulnerabilityStatus {
        NOT_SET("Not set"), NOT_AFFECTED("Not affected"), FALSE_POSITIVE(
                "False positive"), IN_TRIAGE(
                        "In triage"), EXPLOITABLE("Exploitable");

        private String caption;

        VulnerabilityStatus(String caption) {
            this.caption = caption;
        }

        @Override
        public String toString() {
            return caption;
        }
    }

    /**
     * Data object to store the analysis status of a single vulnerability.
     */
    public static class Vulnerability {

        private String id;

        private Instant updated;

        private String developerAnalysis;

        private VulnerabilityStatus status;

        /**
         * Gets the vulnerability ID.
         *
         * @return the vulnerability ID
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the vulnerability ID.
         *
         * @param id
         *            the vulnerability ID
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Gets the timestamp of the most recent analysis update.
         *
         * @return the timestamp
         */
        public Instant getUpdated() {
            return updated;
        }

        /**
         * Sets the timestamp of the most recent analysis update.
         *
         * @param updated
         *            the timestamp
         */
        public void setUpdated(Instant updated) {
            this.updated = updated;
        }

        /**
         * Gets the developer analysis.
         *
         * @return the developer analysis
         */
        public String getDeveloperAnalysis() {
            return developerAnalysis;
        }

        /**
         * Sets the developer analysis.
         *
         * @param developerAnalysis
         *            the developer analysis
         */
        public void setDeveloperAnalysis(String developerAnalysis) {
            this.developerAnalysis = developerAnalysis;
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public VulnerabilityStatus getStatus() {
            return status;
        }

        /**
         * Sets the status.
         *
         * @param status
         *            the status
         */
        public void setStatus(VulnerabilityStatus status) {
            this.status = status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Vulnerability)) {
                return false;
            }
            Vulnerability other = (Vulnerability) obj;
            return Objects.equals(id, other.id);
        }
    }

    private Instant lastScan;

    private Map<String, Vulnerability> vulnerabilities = new HashMap<>();

    /**
     * Gets the timestamp of the most recent vulnerability scan.
     *
     * @return the timestamp
     */
    public Instant getLastScan() {
        return lastScan;
    }

    /**
     * Sets the timestamp of the most recent vulnerability scan.
     *
     * @param lastScan
     *            the timestamp
     */
    public void setLastScan(Instant lastScan) {
        this.lastScan = lastScan;
    }

    /**
     * Gets the vulnerabilities.
     *
     * @return the vulnerabilities
     */
    public Map<String, Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    /**
     * Sets the vulnerabilities.
     *
     * @param vulnerabilities
     *            the vulnerabilities
     */
    public void setVulnerabilities(Map<String, Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
