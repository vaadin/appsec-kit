package com.vaadin.appsec.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinServiceInitListener;

public class UpdateVulnerabilitiesTaskInitListener implements VaadinServiceInitListener {

    private static final long ONE_DAY_PERIOD = 1000L * 60L * 60L * 24L;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new UpdateVulnerabilitiesTask(),
                ONE_DAY_PERIOD, ONE_DAY_PERIOD, TimeUnit.MILLISECONDS);
    }
}
