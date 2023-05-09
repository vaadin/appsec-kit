package com.vaadin.appsec.service;

import org.cyclonedx.model.Bom;

public class BillOfMaterialsService {

    private Bom bom;

    private static final class InstanceHolder {
        static final BillOfMaterialsService instance = new BillOfMaterialsService();
    }

    public static BillOfMaterialsService getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Bom bom) {
        this.bom = bom;
    }

    public Bom getBom() {
        return this.bom;
    }
}
