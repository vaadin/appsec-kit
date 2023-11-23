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

    private final VerticalLayout mainContent;

    private Anchor exportLink;

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
