package com.vaadin.appsec.ui.content;

import com.vaadin.appsec.ui.AppSecDialog;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class ResultsTab extends AbstractAppSecContent {
    private VulnerabilitiesTab vulnerabilitiesTab;
    private DependenciesTab dependenciesTab;

    public ResultsTab() {
        buildLayout();
    }

    private void buildLayout() {
        setSizeFull();

        Label scanResultsTitle = new Label("Scan results");
        scanResultsTitle.addStyleName("scan-results");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.addSelectedTabChangeListener(e -> {
            Component tab = tabSheet.getSelectedTab();
            if (tab instanceof AbstractAppSecContent) {
                ((AbstractAppSecContent) tab).refresh();
            }
        });

        vulnerabilitiesTab = new VulnerabilitiesTab();
        dependenciesTab = new DependenciesTab();

        tabSheet.addTab(vulnerabilitiesTab, "Vulnerabilities");
        tabSheet.addTab(dependenciesTab, "Dependencies");

        addComponents(scanResultsTitle, tabSheet);
        setExpandRatio(tabSheet, 1);
    }

    @Override
    public void refresh() {
        // TODO
    }
}
