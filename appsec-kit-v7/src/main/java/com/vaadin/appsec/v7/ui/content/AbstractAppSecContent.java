/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.ui.content;

import com.vaadin.appsec.v7.data.SeverityLevel;
import com.vaadin.data.util.BeanItemContainer;
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

    public AbstractAppSecContent() {
        setSizeFull();
        setMargin(true);
        setSpacing(true);
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
        filterBar.setSpacing(true);
        filterBar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        filterBar.setWidth("100%");

        filterBar.addComponents(filters);
        filterBar.setExpandRatio(filterBar.getComponent(filters.length - 1), 1);

        filterBar.addComponents(buildClearButton(), buildFilterButton());

        addComponent(filterBar);
    }

    protected BeanItemContainer<SeverityLevel> buildSeverityContainer() {
        BeanItemContainer<SeverityLevel> cont = new BeanItemContainer<>(
                SeverityLevel.class);
        cont.addBean(SeverityLevel.NA);
        cont.addBean(SeverityLevel.LOW);
        cont.addBean(SeverityLevel.MEDIUM);
        cont.addBean(SeverityLevel.HIGH);
        return cont;
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
}
