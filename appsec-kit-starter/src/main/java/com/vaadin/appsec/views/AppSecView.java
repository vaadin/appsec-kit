/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.views;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * AppSec view is the main view for the AppSec Kit.
 */
@PageTitle("AppSec Kit")
@CssImport("./appsec-kit.css")
public class AppSecView extends AbstractAppSecView {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecView.class);

    private VulnerabilitiesView vulnerabilitiesView;
    private DependenciesView dependenciesView;
    private Tabs tabs;
    private Tab vulnerabilitiesTab;
    private Tab dependenciesTab;
    private VerticalLayout tabContent;

    private Span lastScannedLabel;
    private DateFormat formatter;
    private Registration scanListener;
    private Button scanNowButton;

    public AppSecView() {
        setSizeFull();
        addClassNames("appsec-kit-root-layout", "small-margin");
        buildLayout();
        formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                DateFormat.DEFAULT, UI.getCurrent().getLocale());
        refresh();
    }

    private void buildLayout() {
        buildHeaderBar();
        buildTabs();
    }

    private void buildHeaderBar() {
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.setWidth(100, Unit.PERCENTAGE);
        headerBar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        headerBar.addAndExpand(buildAppTitle());
        headerBar.add(buildLastScannedLabel());
        headerBar.add(buildScanNowButton());
        getMainContent().add(headerBar);
    }

    private Component buildAppTitle() {
        Span appTitle = new Span("AppSec Kit");
        appTitle.addClassName("appsec-kit-title");
        appTitle.setSizeFull();
        return appTitle;
    }

    private Component buildLastScannedLabel() {
        lastScannedLabel = new Span();
        lastScannedLabel.addClassName("last-scanned-label");
        lastScannedLabel.setWidth(380, Unit.PIXELS);
        return lastScannedLabel;
    }

    private Component buildScanNowButton() {
        scanNowButton = new Button("Scan now");
        scanNowButton.setWidth("130px");
        scanNowButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        scanNowButton.setDisableOnClick(true);
        scanNowButton.getElement().setAttribute("aria-label", "Scan now");
        scanNowButton.addClickListener(e -> {
            lastScannedLabel.setText("Scanning...");
            UI ui = UI.getCurrent();
            AppSecService.getInstance().scanForVulnerabilities()
                    .exceptionally(ex -> {
                        LOGGER.error("Error scanning vulnerabilities.", ex);
                        ui.access(() -> {
                            lastScannedLabel
                                    .setText("Error scanning vulnerabilities.");
                            scanNowButton.setEnabled(true);
                        });
                        return null;
                    });
        });
        return scanNowButton;
    }

    private void buildTabs() {
        vulnerabilitiesView = new VulnerabilitiesView(this);
        dependenciesView = new DependenciesView(this);
        vulnerabilitiesTab = new Tab("Vulnerabilities");
        dependenciesTab = new Tab("Dependencies");

        tabs = new Tabs(vulnerabilitiesTab, dependenciesTab);
        tabs.addSelectedChangeListener(e -> setTabContent(e.getSelectedTab()));

        tabContent = new VerticalLayout();
        tabContent.setSizeFull();
        tabContent.setMargin(false);
        tabContent.setPadding(false);
        tabContent.setSpacing(false);
        setTabContent(tabs.getSelectedTab());

        getMainContent().addAndExpand(tabs, tabContent);
    }

    private void setTabContent(Tab tab) {
        tabContent.removeAll();

        if (tab.equals(vulnerabilitiesTab)) {
            tabContent.add(vulnerabilitiesView);
            vulnerabilitiesView.refresh();
        } else if (tab.equals(dependenciesTab)) {
            tabContent.add(dependenciesView);
            dependenciesView.refresh();
        }
    }

    @Override
    public void refresh() {
        setTabContent(tabs.getSelectedTab());
        Instant lastScan = AppSecService.getInstance().refresh().getLastScan();
        lastScannedLabel.setText("Last Scan: " + (lastScan == null ? "--"
                : formatter.format(Date.from(lastScan))));
    }

    void showVulnerabilitiesTabFor(Dependency item) {
        tabs.setSelectedTab(vulnerabilitiesTab);
        vulnerabilitiesView.filterOn(item);
    }

    @Override
    public void onAttach(AttachEvent event) {
        super.onAttach(event);
        removeScanListener();
        scanListener = AppSecService.getInstance()
                .addScanEventListener(this::handleScanEvent);
        LOGGER.debug("Scan event listener added");
    }

    @Override
    public void onDetach(DetachEvent event) {
        super.onDetach(event);
        removeScanListener();
    }

    private void removeScanListener() {
        if (scanListener != null) {
            scanListener.remove();
            scanListener = null;
        }
    }

    private void handleScanEvent(AppSecScanEvent event) {
        getUI().ifPresent(this::handleScanNowButton);
    }

    private void handleScanNowButton(UI ui) {
        ui.access(() -> {
            scanNowButton.setEnabled(true);
            refresh();
            if (PushMode.MANUAL == ui.getPushConfiguration().getPushMode()) {
                ui.push();
            }
        });
    }
}
