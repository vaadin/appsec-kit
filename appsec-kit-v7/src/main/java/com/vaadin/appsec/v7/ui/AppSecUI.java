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

import com.vaadin.annotations.StyleSheet;
import com.vaadin.appsec.v7.ui.content.AbstractAppSecContent;
import com.vaadin.appsec.v7.ui.content.ResultsTab;
import com.vaadin.appsec.v7.ui.content.StatusTab;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * UI class for displaying main content of AppSec Kit.
 */
@StyleSheet("appsec-v7.css")
public class AppSecUI extends UI {

    private StatusTab statusTab;
    private ResultsTab resultsTab;

    private void setup() {
        setSizeFull();
        getPage().setTitle("Vaadin AppSec Kit");
    }

    private void buildLayout() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.addSelectedTabChangeListener(e -> {
            Component tab = tabSheet.getSelectedTab();
            if (tab instanceof AbstractAppSecContent) {
                ((AbstractAppSecContent) tab).refresh();
            }
        });

        statusTab = new StatusTab();
        resultsTab = new ResultsTab();

        tabSheet.addTab(statusTab, "Status");
        tabSheet.addTab(resultsTab, "Results");

        addStyleName("appsec-kit-dialog");

        VerticalLayout wrapper = new VerticalLayout(tabSheet);
        wrapper.setSizeFull();
        wrapper.setMargin(true);
        wrapper.setSpacing(true);
        setContent(wrapper);
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setup();
        buildLayout();
    }
}
