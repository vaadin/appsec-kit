package com.vaadin.appsec.data;

import java.util.Comparator;

public class SeverityLevelComparator implements Comparator<SeverityLevel> {
    @Override
    public int compare(SeverityLevel o1, SeverityLevel o2) {
        return compareStatic(o1, o2);
    }

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
