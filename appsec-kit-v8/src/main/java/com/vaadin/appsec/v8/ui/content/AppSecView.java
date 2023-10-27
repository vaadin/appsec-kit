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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;

/**
 * AppSec view is the main view for the AppSec Kit.
 */
public class AppSecView extends AbstractAppSecContent {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecView.class);

    private VulnerabilitiesTab vulnerabilitiesTab;

    private DependenciesTab dependenciesTab;

    private TabSheet tabSheet;

    private Label lastScannedLabel;

    private DateFormat formatter;
    private Registration scanListener;
    private Button scanNow;

    /**
     * Instantiates a new AppSec view.
     */
    public AppSecView() {
        buildLayout();
        formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                DateFormat.DEFAULT, UI.getCurrent().getLocale());
    }

    private void buildLayout() {
        Label appTitle = new Label("AppSec Kit");
        appTitle.addStyleName("appseckit-title");
        appTitle.setSizeFull();

        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.setWidth(100, Unit.PERCENTAGE);
        headerBar.addComponentsAndExpand(appTitle);
        headerBar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        lastScannedLabel = new Label();
        headerBar.addComponent(lastScannedLabel);

        scanNow = new Button("Scan now");
        scanNow.setDisableOnClick(true);
        scanNow.addClickListener(e -> {
            lastScannedLabel.setValue("Scanning...");
            AppSecService.getInstance().scanForVulnerabilities();
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

    void showVulnerabilitiesTabFor(Dependency item) {
        tabSheet.setSelectedTab(vulnerabilitiesTab);
        vulnerabilitiesTab.filterOn(item);
    }

    @Override
    public void attach() {
        super.attach();
        removeScanListener();
        scanListener = AppSecService.getInstance()
                .addScanEventListener(this::handleScanEvent);
        LOGGER.debug("Scan event listener added");
    }

    @Override
    public void detach() {
        super.detach();
        removeScanListener();
    }

    private void removeScanListener() {
        if (scanListener != null) {
            scanListener.remove();
            scanListener = null;
        }
    }

    private void handleScanEvent(AppSecScanEvent event) {
        getUI().access(() -> {
            scanNow.setEnabled(true);
            refresh();
            if (PushMode.MANUAL == getUI().getPushConfiguration()
                    .getPushMode()) {
                getUI().push();
            }
        });
    }
}
