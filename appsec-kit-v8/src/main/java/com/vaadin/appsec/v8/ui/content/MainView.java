/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui.content;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.service.VulnerabilityStore;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;

/**
 * Results tab content
 */
public class MainView extends AbstractAppSecContent {
    private VulnerabilitiesTab vulnerabilitiesTab;
    private DependenciesTab dependenciesTab;
    private TabSheet tabSheet;

    private Label lastScannedLabel;

    private DateFormat formatter;

    /**
     * Instantiates a new Results tab.
     */
    public MainView() {
        buildLayout();
        formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                DateFormat.DEFAULT, UI.getCurrent().getLocale());
    }

    private void buildLayout() {
        Label appTitle = new Label("Vaadin AppSec Kit");
        appTitle.addStyleName("appseckit-title");
        appTitle.setSizeFull();

        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.setWidth(100, Unit.PERCENTAGE);
        headerBar.addComponentsAndExpand(appTitle);
        headerBar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        lastScannedLabel = new Label();
        headerBar.addComponent(lastScannedLabel);

        UI ui = UI.getCurrent();
        Button scanNow = new Button("Scan now");
        scanNow.setDisableOnClick(true);
        scanNow.addClickListener(e -> {
            lastScannedLabel.setValue("Scanning...");
            // TODO Needs to be refactored when listener mechanism is in place
            new Thread(() -> {
                VulnerabilityStore.getInstance().refresh(() -> {
                    ui.access(() -> {
                        scanNow.setEnabled(true);
                        refresh();
                        ui.push();
                    });
                });
            }).start();
        });
        headerBar.addComponent(scanNow);

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

        getMainContent().addComponents(headerBar, tabSheet);
        getMainContent().setExpandRatio(tabSheet, 1);
    }

    @Override
    public void refresh() {
        Component selectedTab = tabSheet.getSelectedTab();
        if (selectedTab instanceof AbstractAppSecContent) {
            ((AbstractAppSecContent) selectedTab).refresh();
        }
        Instant lastScan = AppSecService.getInstance().refresh().getLastScan();
        lastScannedLabel.setValue("Last Scan: " + (lastScan == null ? "--"
                : formatter.format(Date.from(lastScan))));
    }

    void showVulnerabilitiesTabFor(DependencyDTO item) {
        tabSheet.setSelectedTab(vulnerabilitiesTab);
        vulnerabilitiesTab.filterOn(item);
    }
}
