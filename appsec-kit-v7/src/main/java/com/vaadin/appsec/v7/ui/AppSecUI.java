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

import com.vaadin.annotations.Push;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.appsec.v7.ui.content.MainView;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 * UI class for displaying main content of AppSec Kit.
 */
@StyleSheet("appsec-v7.css")
@Push
public class AppSecUI extends UI {

    private MainView mainView;

    private void setup() {
        setSizeFull();
        getPage().setTitle("Vaadin AppSec Kit");
    }

    private void buildLayout() {
        mainView = new MainView();
        mainView.addStyleName("appsec-kit-root-layout");
        mainView.addStyleName("small-margin");
        setContent(mainView);
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setup();
        buildLayout();
        mainView.refresh();
    }
}
