package com.vaadin.appsec.service;

import org.cyclonedx.model.Bom;

public class BOMService {

    private Bom bom;

    private static final class InstanceHolder {
        static final BOMService instance = new BOMService();
    }

    public static BOMService getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Bom bom) {
        this.bom = bom;
    }

    public Bom getBom() {
        return this.bom;
    }
}
