package com.vaadin.appsec.client;

import com.vaadin.shared.communication.ServerRpc;

public interface AppSecServerRpc extends ServerRpc {

    void fetchDependencies();
}
