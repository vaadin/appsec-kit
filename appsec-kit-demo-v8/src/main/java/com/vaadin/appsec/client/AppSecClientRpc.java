package com.vaadin.appsec.client;

import java.util.List;

import com.vaadin.appsec.client.data.Dependency;
import com.vaadin.shared.communication.ClientRpc;

public interface AppSecClientRpc extends ClientRpc {
    void setDependencies(List<Dependency> dependencies);
}
