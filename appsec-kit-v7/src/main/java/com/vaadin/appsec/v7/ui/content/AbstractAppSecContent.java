/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.ui.content;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.analysis.AssessmentStatus;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
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

    private final VerticalLayout mainContent;

    public AbstractAppSecContent() {
        super();
        setSizeFull();
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setMargin(false);
        mainContent.setSpacing(true);
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
        filterBar.setSpacing(true);
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
        addComponent(detailsContent);
        setExpandRatio(detailsContent, 1);
    }

    protected void showMainContent() {
        removeAllComponents();
        addComponent(mainContent);
        setExpandRatio(mainContent, 1);
    }

    static BeanItemContainer<SeverityLevel> buildSeverityContainer() {
        BeanItemContainer<SeverityLevel> cont = new BeanItemContainer<>(
                SeverityLevel.class);
        cont.addBean(SeverityLevel.NA);
        cont.addBean(SeverityLevel.LOW);
        cont.addBean(SeverityLevel.MEDIUM);
        cont.addBean(SeverityLevel.HIGH);
        return cont;
    }

    static BeanItemContainer<AssessmentStatus> buildVaadinAnalysisStatusContainer() {
        BeanItemContainer<AssessmentStatus> cont = new BeanItemContainer<>(
                AssessmentStatus.class);
        cont.addBean(AssessmentStatus.TRUE_POSITIVE);
        cont.addBean(AssessmentStatus.FALSE_POSITIVE);
        cont.addBean(AssessmentStatus.UNDER_REVIEW);
        return cont;
    }

    static BeanItemContainer<AppSecData.VulnerabilityStatus> buildDevAnalysisStatusContainer() {
        BeanItemContainer<AppSecData.VulnerabilityStatus> cont = new BeanItemContainer<>(
                AppSecData.VulnerabilityStatus.class);
        cont.addBean(AppSecData.VulnerabilityStatus.NOT_SET);
        cont.addBean(AppSecData.VulnerabilityStatus.NOT_AFFECTED);
        cont.addBean(AppSecData.VulnerabilityStatus.FALSE_POSITIVE);
        cont.addBean(AppSecData.VulnerabilityStatus.IN_TRIAGE);
        cont.addBean(AppSecData.VulnerabilityStatus.EXPLOITABLE);
        return cont;
    }

}
