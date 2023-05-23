/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.data;

import java.util.Comparator;

/**
 * Comparator for SeverityLevel objects; for sorting in a grid
 */
public class SeverityLevelComparator implements Comparator<SeverityLevel> {
    @Override
    public int compare(SeverityLevel o1, SeverityLevel o2) {
        return compareStatic(o1, o2);
    }

    /**
     * Compare static int.
     *
     * @param o1
     *            the o 1
     * @param o2
     *            the o 2
     * @return the int
     */
    public static int compareStatic(SeverityLevel o1, SeverityLevel o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else if (o1.ordinal() > o2.ordinal()) {
            return -1;
        } else if (o1.ordinal() < o2.ordinal()) {
            return 1;
        } else {
            return 0;
        }
    }
}
