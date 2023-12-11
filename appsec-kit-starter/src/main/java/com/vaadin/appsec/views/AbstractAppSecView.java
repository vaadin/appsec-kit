/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

/**
 * Abstract AppSec view is a base class for the view parts.
 */
public abstract class AbstractAppSecView extends VerticalLayout {

    static final String DEPENDENCY = "Dependency";
    static final String DEPENDENCY_GROUP = "Dependency group";
    static final String DEPENDENCY_NAME = "Dependency name";
    static final String ECOSYSTEM = "Ecosystem";
    static final String SEVERITY = "Severity";
    static final String CVSS_SCORE = "CVSS score";
    static final String VAADIN_ANALYSIS = "Vaadin analysis";
    static final String DEVELOPER_ANALYSIS = "Developer analysis";
    static final String IS_DEVELOPMENT = "Is development?";
    static final String NUMBER_OF_VULNERABILITIES = "# of vulnerabilities";
    static final String HIGHEST_SEVERITY = "Highest severity";
    static final String HIGHEST_CVSS_SCORE = "Highest CVSS score";
    static final String VERSION = "Version";
    static final String SEARCH = "Search";
    static final String SHOW_DETAILS = "Show details";
    static final String VULNERABILITY_NAME_OR_IDENTIFIER = "Vulnerability name or identifier";

    private final VerticalLayout mainContent;

    protected Anchor exportLink;

    AbstractAppSecView() {
        super();
        setSizeFull();
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setMargin(false);
        showMainContent();
    }

    abstract void refresh();

    Component buildFilterBar(Component... filters) {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidth(100, Unit.PERCENTAGE);
        filterBar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        filterBar.add(filters);
        filterBar.expand(filters);
        filterBar.add(buildClearButton());
        filterBar.add(buildExportButton());
        return filterBar;
    }

    Button buildClearButton() {
        Button clear = new Button("Clear");
        clear.addClickListener(e -> clearFilters());
        return clear;
    }

    Anchor buildExportButton() {
        exportLink = new Anchor();
        exportLink.getElement().setAttribute("download", true);
        exportLink.add(new Button("Export", VaadinIcon.DOWNLOAD.create()));
        return exportLink;
    }

    void updateExportData(StreamResource streamResource) {
        exportLink.setHref(streamResource);
    }

    void clearFilters() {
    }

    void applyFilters() {
    }

    void showDetails(Component detailsContent) {
        removeAll();
        addAndExpand(detailsContent);
    }

    VerticalLayout getMainContent() {
        return mainContent;
    }

    void showMainContent() {
        removeAll();
        addAndExpand(mainContent);
    }
}
