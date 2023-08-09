/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model.analysis;

/**
 * Status of a vulnerability assessment.
 */
public enum AssessmentStatus {

    /**
     * The vulnerability is a true positive.
     */
    TRUE_POSITIVE("True Positive"),

    /**
     * The vulnerability is a false positive.
     */
    FALSE_POSITIVE("False Positive"),

    /**
     * The vulnerability is under review.
     */
    UNDER_REVIEW("Under Review");

    private final String caption;

    AssessmentStatus(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
