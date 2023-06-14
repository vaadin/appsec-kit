/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui.content;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Abstract app sec content is a base class for view parts.
 */
public abstract class AbstractAppSecContent extends VerticalLayout {

    private final VerticalLayout mainContent;

    public AbstractAppSecContent() {
        super();
        setSizeFull();
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setMargin(false);
        showMainContent();
    }

    /**
     * Refresh.
     */
    public abstract void refresh();

    /**
     * Build clear button.
     *
     * @return the button
     */
    protected Button buildClearButton() {
        Button clear = new Button("Clear");
        clear.addClickListener(e -> clearFilters());
        return clear;
    }

    /**
     * Build filter button.
     *
     * @return the button
     */
    protected Button buildFilterButton() {
        Button filter = new Button("Filter");
        filter.addClickListener(e -> applyFilters());
        filter.addStyleName(ValoTheme.BUTTON_PRIMARY);
        return filter;
    }

    /**
     * Build filter bar.
     *
     * @param filters
     *            the filters
     */
    protected void buildFilterBar(Component... filters) {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        filterBar.setWidth(100, Unit.PERCENTAGE);

        filterBar.addComponents(filters);
        filterBar.setExpandRatio(filterBar.getComponent(filters.length - 1), 1);

        filterBar.addComponents(buildClearButton(), buildFilterButton());

        mainContent.addComponent(filterBar);
    }

    VerticalLayout getMainContent() {
        return mainContent;
    }

    /**
     * Apply filters.
     */
    protected void applyFilters() {
    }

    /**
     * Clear filters.
     */
    protected void clearFilters() {
    }

    protected void showDetails(Component detailsContent) {
        removeAllComponents();
        addComponentsAndExpand(detailsContent);
    }

    protected void showMainContent() {
        removeAllComponents();
        addComponentsAndExpand(mainContent);
    }
}
