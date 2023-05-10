package com.vaadin.appsec.service;

import java.util.List;

import org.cyclonedx.model.Component;

import com.vaadin.appsec.model.osv.response.OpenSourceVulnerability;

public class UpdateVulnerabilitiesTask implements Runnable {

    @Override
    public void run() {
        OpenSourceVulnerabilityService osvService = new OpenSourceVulnerabilityService();
        List<Component> components = BillOfMaterialsStore.getInstance().getBom().getComponents();
        List<OpenSourceVulnerability> vulnerabilities = osvService.getVulnerabilities(components);
        VulnerabilityStore.getInstance().addVulnerabilities(vulnerabilities);
    }
}
