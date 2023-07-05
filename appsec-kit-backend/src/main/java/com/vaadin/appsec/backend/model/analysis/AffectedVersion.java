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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The assessment for a specific version (or range) affected by a vulnerability.
 */
public class AffectedVersion implements Serializable {

    @JsonIgnore
    private String versionRange;

    private AssessmentStatus status;

    private String comment;

    private String fixedIn;

    /**
     * The range of versions affected by this vulnerability.
     *
     * @return the affected version range
     */
    public String getVersionRange() {
        return versionRange;
    }

    void setVersionRange(String versionRange) {
        this.versionRange = versionRange;
    }

    /**
     * The current status of this assessment.
     *
     * @return the assessment status
     */
    public AssessmentStatus getStatus() {
        return status;
    }

    void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    /**
     * A textual comment with additional information about this assessment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The version where the vulnerability is not present anymore.
     *
     * @return the fixed version
     */
    public String getFixedIn() {
        return fixedIn;
    }

    void setFixedIn(String fixedIn) {
        this.fixedIn = fixedIn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionRange);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffectedVersion)) {
            return false;
        }
        AffectedVersion other = (AffectedVersion) obj;
        return Objects.equals(versionRange, other.versionRange);
    }

    @Override
    public String toString() {
        return "AffectedVersion{" + "versionRange='" + versionRange + '\''
                + ", status=" + status + ", comment='" + comment + '\''
                + ", fixedIn='" + fixedIn + '\'' + '}';
    }
}
