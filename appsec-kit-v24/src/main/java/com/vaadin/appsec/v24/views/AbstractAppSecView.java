/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.v24.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Abstract AppSec view is a base class for the view parts.
 */
public abstract class AbstractAppSecView extends VerticalLayout {

    private final VerticalLayout mainContent;

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
        filterBar.add(buildClearButton(), buildFilterButton());
        return filterBar;
    }

    Button buildClearButton() {
        Button clear = new Button("Clear");
        clear.addClickListener(e -> clearFilters());
        return clear;
    }

    void clearFilters() {
    }

    Button buildFilterButton() {
        Button filter = new Button("Filter");
        filter.addClickListener(e -> applyFilters());
        filter.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return filter;
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
