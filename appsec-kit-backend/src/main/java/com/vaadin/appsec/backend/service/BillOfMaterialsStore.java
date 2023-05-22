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

public class BillOfMaterialsStore {

    private Bom bom;

    private static final class InstanceHolder {
        static final BillOfMaterialsStore instance = new BillOfMaterialsStore();
    }

    public static BillOfMaterialsStore getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Bom bom) {
        this.bom = bom;
    }

    public Bom getBom() {
        return this.bom;
    }
}
