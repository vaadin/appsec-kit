/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.model.dto;

import java.util.Comparator;

/**
 * Comparator for severity levels.
 */
public class SeverityLevelComparator implements Comparator<SeverityLevel> {

    @Override
    public int compare(SeverityLevel o1, SeverityLevel o2) {
        return compareStatic(o1, o2);
    }

    /**
     * Compares severity levels based on their ordinal number.
     *
     * @param o1
     *            the severity level 1
     * @param o2
     *            the severity level 2
     * @return the int result of the comparison
     */
    public static int compareStatic(SeverityLevel o1, SeverityLevel o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else
            return Integer.compare(o2.ordinal(), o1.ordinal());
    }
}
