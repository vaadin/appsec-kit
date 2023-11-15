/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model.dto;

/**
 * Severity level for a vulnerability. Contains the appropriate CVSS score
 * ranges what can be used to get the severity based on the score.
 *
 * @see <a href="https://nvd.nist.gov/vuln-metrics/cvss">CVSS score</a>
 */
public enum SeverityLevel {

    /**
     * Critical severity level.
     */
    CRITICAL("Critical", 9.0d, 10.0d),
    /**
     * High severity level.
     */
    HIGH("High", 7.0d, 8.9d),
    /**
     * Medium severity level.
     */
    MEDIUM("Medium", 4.0d, 6.9d),
    /**
     * Low severity level.
     */
    LOW("Low", 0.1d, 3.9d),
    /**
     * None severity level.
     */
    NONE("None", 0.0d, 0.0d);

    private final String caption;
    private final Double minScore;
    private final Double maxScore;

    SeverityLevel(String caption, Double min, Double maxScore) {
        this.caption = caption;
        this.minScore = min;
        this.maxScore = maxScore;
    }

    public Double getMinScore() {
        return minScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public String toString() {
        return caption;
    }

    /**
     * Returns the severity level based on the CVSS score. The score should be
     * between 0.0 and 10.0 numbers.
     *
     * @param cvssScore
     *            the CVSS score
     * @return the severity level
     */
    public static SeverityLevel getSeverityLevelForCvssScore(Double cvssScore) {
        for (SeverityLevel severityLevel : values()) {
            if (cvssScore >= severityLevel.getMinScore()
                    && cvssScore <= severityLevel.getMaxScore()) {
                return severityLevel;
            }
        }
        throw new IllegalArgumentException(
                "The score is out of the allowed range");
    }
}
