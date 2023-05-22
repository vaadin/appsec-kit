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

public abstract class AbstractAppSecContent extends VerticalLayout {

    public abstract void refresh();

    protected Button buildClearButton() {
        Button clear = new Button("Clear");
        clear.addClickListener(e -> clearFilters());
        return clear;
    }

    protected Button buildFilterButton() {
        Button filter = new Button("Filter");
        filter.addClickListener(e -> applyFilters());
        filter.addStyleName(ValoTheme.BUTTON_PRIMARY);
        return filter;
    }

    protected void buildFilterBar(Component... filters) {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        filterBar.setWidth("100%");

        filterBar.addComponents(filters);
        filterBar.setExpandRatio(filterBar.getComponent(filters.length - 1), 1);

        filterBar.addComponents(buildClearButton(), buildFilterButton());

        addComponent(filterBar);
    }

    protected void applyFilters() {
    }

    protected void clearFilters() {
    }
}
