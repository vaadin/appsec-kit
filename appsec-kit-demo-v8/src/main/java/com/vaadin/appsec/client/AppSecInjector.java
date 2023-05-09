package com.vaadin.appsec.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.debug.internal.VDebugWindow;

public class AppSecInjector implements EntryPoint {
    @Override
    public void onModuleLoad() {
        if (ApplicationConfiguration.isDebugMode()) {
            // Run when debug mode bundle is loaded
            GWT.runAsync(VDebugWindow.class, new RunAsyncCallback() {
                @Override
                public void onSuccess() {
                    VDebugWindow.get().addSection(new DependenciesSection());
                }

                @Override
                public void onFailure(Throwable reason) {
                    // Ignore. Failure will be handled by
                    // ApplicationConfiguration
                }
            });
        }
    }
}
