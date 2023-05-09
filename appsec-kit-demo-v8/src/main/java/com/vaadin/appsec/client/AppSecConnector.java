package com.vaadin.appsec.client;

import java.util.List;

import com.vaadin.appsec.client.data.Dependency;
import com.vaadin.appsec.extension.AppSecUIExtension;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.VConsole;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@Connect(AppSecUIExtension.class)
public class AppSecConnector extends AbstractExtensionConnector {
    private DependenciesSection dependenciesSection;

    @Override
    protected void extend(ServerConnector target) {
        VConsole.error("EXTENSION");
    }

    @Override
    protected void init() {
        super.init();

        registerRpc(AppSecClientRpc.class, new AppSecClientRpc() {
            @Override
            public void setDependencies(List<Dependency> dependencies) {
                dependenciesSection.setDependencies(dependencies);
            }
        });
    }

    void setDependenciesSection(DependenciesSection dependenciesSection) {
        this.dependenciesSection = dependenciesSection;
    }
}
