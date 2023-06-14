/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui.content;

import com.vaadin.appsec.v8.data.DependencyDTO;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;

/**
 * Results tab content
 */
public class ResultsTab extends AbstractAppSecContent {
    private VulnerabilitiesTab vulnerabilitiesTab;
    private DependenciesTab dependenciesTab;
    private TabSheet tabSheet;

    /**
     * Instantiates a new Results tab.
     */
    public ResultsTab() {
        buildLayout();
    }

    private void buildLayout() {
        Label scanResultsTitle = new Label("Scan results");
        scanResultsTitle.addStyleName("scan-results");

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.addSelectedTabChangeListener(e -> {
            Component tab = tabSheet.getSelectedTab();
            if (tab instanceof AbstractAppSecContent) {
                ((AbstractAppSecContent) tab).refresh();
            }
        });

        vulnerabilitiesTab = new VulnerabilitiesTab(this);
        dependenciesTab = new DependenciesTab(this);

        tabSheet.addTab(vulnerabilitiesTab, "Vulnerabilities");
        tabSheet.addTab(dependenciesTab, "Dependencies");

        getMainContent().addComponents(scanResultsTitle, tabSheet);
        getMainContent().setExpandRatio(tabSheet, 1);
    }

    @Override
    public void refresh() {
        Component selectedTab = tabSheet.getSelectedTab();
        if (selectedTab instanceof AbstractAppSecContent) {
            ((AbstractAppSecContent) selectedTab).refresh();
        }
    }

    void showVulnerabilitiesTabFor(DependencyDTO item) {
        tabSheet.setSelectedTab(vulnerabilitiesTab);
        vulnerabilitiesTab.filterOn(item);
    }
}
