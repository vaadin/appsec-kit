package com.vaadin.appsec.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.appsec.data.DependencyDTO;
import com.vaadin.appsec.service.BillOfMaterialsStore;

public class AppSecDataProvider {

    public static List<DependencyDTO> getDependencies() {
        // TODO Needs to include missing properties to deps as well (severity, risk score, # of vulnerabilities)
        return BillOfMaterialsStore.getInstance().getBom().getComponents().stream()
                .map(c -> new DependencyDTO(c.getGroup(), c.getName(), c.getVersion()))
                .collect(Collectors.toList());
    }
}
