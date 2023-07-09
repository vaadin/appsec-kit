/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend.model.analysis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AffectedVersionTest {

    @Test
    public void isInRage_simpleVersion() {
        AffectedVersion version = new AffectedVersion();
        version.setVersionRange("[1.0.0]");

        assertTrue(version.isInRange("1.0.0"));
        assertFalse(version.isInRange("1.1.0"));
    }

    @Test
    public void isInRage_singleRangedVersion() {
        AffectedVersion version = new AffectedVersion();
        version.setVersionRange("[1.0.0,1.1.0)");

        assertTrue(version.isInRange("1.0.0"));
        assertFalse(version.isInRange("1.1.0"));
    }

    @Test
    public void isInRage_multipleRangeVersion() {
        AffectedVersion version = new AffectedVersion();
        version.setVersionRange("[1.0.0,1.1.0),[2.0.0,2.1.0)");

        assertTrue(version.isInRange("1.0.0"));
        assertFalse(version.isInRange("1.1.0"));
        assertTrue(version.isInRange("2.0.0"));
        assertFalse(version.isInRange("2.1.0"));
    }
}
