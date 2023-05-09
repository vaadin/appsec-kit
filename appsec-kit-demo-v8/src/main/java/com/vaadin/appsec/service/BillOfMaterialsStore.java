package com.vaadin.appsec.service;

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
