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
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * AppSec view is the main view for the AppSec Kit.
 */
@PageTitle("AppSec Kit")
@CssImport("./appsec-kit.css")
@AnonymousAllowed
public class AppSecView extends AbstractAppSecView {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecView.class);

    private VulnerabilitiesTab vulnerabilitiesTab;
    private DependenciesTab dependenciesTab;
    private TabSheet tabSheet;
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
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.setWidth(100, Unit.PERCENTAGE);
        headerBar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        headerBar.addAndExpand(buildAppTitle());
        headerBar.add(buildLastScannedLabel());
        headerBar.add(buildScanNowButton());

        getMainContent().add(headerBar);
        getMainContent().addAndExpand(buildTabSheet());
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
        scanNowButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        scanNowButton.setDisableOnClick(true);
        scanNowButton.addClickListener(e -> {
            lastScannedLabel.setText("Scanning...");
            AppSecService.getInstance().scanForVulnerabilities();
        });
        return scanNowButton;
    }

    private Component buildTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addSelectedChangeListener(e -> {
            Component component = tabSheet
                    .getComponent(tabSheet.getSelectedTab());
            if (component instanceof AbstractAppSecView abstractAppSecView) {
                abstractAppSecView.refresh();
            }
        });
        vulnerabilitiesTab = new VulnerabilitiesTab(this);
        dependenciesTab = new DependenciesTab(this);
        tabSheet.add("Vulnerabilities", vulnerabilitiesTab);
        tabSheet.add("Dependencies", dependenciesTab);
        return tabSheet;
    }

    @Override
    public void refresh() {
        Component component = tabSheet.getComponent(tabSheet.getSelectedTab());
        if (component instanceof AbstractAppSecView abstractAppSecView) {
            abstractAppSecView.refresh();
        }
        Instant lastScan = AppSecService.getInstance().refresh().getLastScan();
        lastScannedLabel.setText("Last Scan: " + (lastScan == null ? "--"
                : formatter.format(Date.from(lastScan))));
    }

    void showVulnerabilitiesTabFor(Dependency item) {
        tabSheet.setSelectedTab(tabSheet.getTab(vulnerabilitiesTab));
        vulnerabilitiesTab.filterOn(item);
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
            LOGGER.debug("Scan event listener removed");
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
