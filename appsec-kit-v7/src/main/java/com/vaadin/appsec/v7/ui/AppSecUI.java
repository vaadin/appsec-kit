/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.v7.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.appsec.v7.ui.content.AppSecView;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.UI;

/**
 * UI class for displaying main content of AppSec Kit.
 */
@StyleSheet("appsec-v7.css")
@Push(transport = Transport.LONG_POLLING)
@PreserveOnRefresh
public class AppSecUI extends UI {

    private AppSecView appSecView;

    private void setup() {
        setSizeFull();
        getPage().setTitle("AppSec Kit");
    }

    private void buildLayout() {
        appSecView = new AppSecView();
        appSecView.addStyleName("appsec-kit-root-layout");
        appSecView.addStyleName("small-margin");
        setContent(appSecView);
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setup();
        buildLayout();
        appSecView.refresh();
    }
}