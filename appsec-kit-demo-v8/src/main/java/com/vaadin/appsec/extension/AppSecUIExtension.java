package com.vaadin.appsec.extension;

import java.util.ArrayList;
import java.util.List;

import org.cyclonedx.model.Component;

import com.vaadin.appsec.client.AppSecClientRpc;
import com.vaadin.appsec.client.AppSecServerRpc;
import com.vaadin.appsec.client.data.Dependency;
import com.vaadin.appsec.service.BillOfMaterialsStore;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

public class AppSecUIExtension extends AbstractExtension {

    private AppSecServerRpc serverRpc;

    public AppSecUIExtension() {
        serverRpc = new AppSecServerRpc() {
            @Override
            public void fetchDependencies() {
                List<Dependency> deps = new ArrayList<>();
                for (Component c : BillOfMaterialsStore.getInstance().getBom().getComponents()) {
                    deps.add(new Dependency(c.getGroup(), c.getName(), c.getVersion()));
                }
                getRpcProxy(AppSecClientRpc.class).setDependencies(deps);
            }
        };
    }

    public void extend(UI ui) {
        super.extend(ui);
        registerRpc(serverRpc);
    }
}
