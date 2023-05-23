/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend.service;

import org.cyclonedx.model.Bom;

/**
 * Provides means to store and fetch bill of materials from a static instance.
 */
public class BillOfMaterialsStore {

    private Bom bom;

    private static final class InstanceHolder {
        static final BillOfMaterialsStore instance = new BillOfMaterialsStore();
    }

    /**
     * @return static BillOfMaterialsStore instance
     */
    public static BillOfMaterialsStore getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Sets the given Bom to the static store.
     *
     * @param bom
     *            Bom to be stored
     */
    public void init(Bom bom) {
        this.bom = bom;
    }

    /**
     * @return currently stored Bom
     */
    public Bom getBom() {
        return this.bom;
    }
}
