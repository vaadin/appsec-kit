/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui;

import com.vaadin.appsec.v8.ui.content.AbstractAppSecContent;
import com.vaadin.appsec.v8.ui.content.ResultsTab;
import com.vaadin.appsec.v8.ui.content.StatusTab;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

/**
 * Main dialog window.
 */
public class AppSecDialog extends Window {

    private StatusTab statusTab;
    private ResultsTab resultsTab;

    /**
     * Instantiates a new App sec dialog.
     */
    public AppSecDialog() {
        setup();
        buildLayout();
    }

    private void setup() {
        setModal(true);
        setClosable(true);
        setResizable(true);
        setWidth(85, Unit.PERCENTAGE);
        setHeight(85, Unit.PERCENTAGE);
        center();
        setCaption("Vaadin AppSec Kit");
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

        setContent(tabSheet);
    }
}
