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

import java.util.stream.Collectors;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.dto.VulnerabilityDTO;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

/**
 * Vulnerabilities tab content
 */
public class VulnerabilitiesTab extends AbstractAppSecContent {
    private Grid grid;
    private ComboBox dependency;
    private ComboBox severity;
    private ComboBox vaadinAnalysis;
    private ComboBox devAnalysis;
    private MainView parent;

    /**
     * Instantiates a new Vulnerabilities tab.
     */
    public VulnerabilitiesTab(MainView parent) {
        this.parent = parent;
        buildFilters();
        buildGrid();
        setMargin(true);
    }

    private void buildFilters() {
        dependency = new ComboBox("Dependency");

        vaadinAnalysis = new ComboBox("Vaadin analysis");
        // TODO Set vaadin analysis options

        devAnalysis = new ComboBox("Developer analysis");
        devAnalysis.setContainerDataSource(
                AbstractAppSecContent.buildDevStatusContainer());

        severity = new ComboBox("Severity level");
        severity.setContainerDataSource(
                AbstractAppSecContent.buildSeverityContainer());

        buildFilterBar(dependency, vaadinAnalysis, devAnalysis, severity);
    }

    @Override
    protected void clearFilters() {
        dependency.setValue(null);
        vaadinAnalysis.setValue(null);
        devAnalysis.setValue(null);
        severity.setValue(null);
        getContainer().removeAllContainerFilters();
    }

    @Override
    protected void applyFilters() {
        getContainer().removeAllContainerFilters();

        final DependencyDTO dependencyFilter = (DependencyDTO) dependency
                .getValue();
        final SeverityLevel severityFilter = (SeverityLevel) severity
                .getValue();

        // TODO Add filtering for vaadin analysis

        final AppSecData.VulnerabilityStatus devAnalysisFilter = (AppSecData.VulnerabilityStatus) devAnalysis
                .getValue();

        getContainer().addContainerFilter(new Container.Filter() {
            @Override
            public boolean passesFilter(Object itemId, Item item)
                    throws UnsupportedOperationException {
                VulnerabilityDTO vulnerabilityDTO = (VulnerabilityDTO) itemId;
                if (dependencyFilter != null && !dependencyFilter
                        .equals(vulnerabilityDTO.getDependency())) {
                    return false;
                }
                if (severityFilter != null && !severityFilter
                        .equals(vulnerabilityDTO.getSeverityLevel())) {
                    return false;
                }
                if (devAnalysisFilter != null && !devAnalysisFilter
                        .equals(vulnerabilityDTO.getDeveloperStatus())) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return "dependency".equals(propertyId)
                        || "severityLevel".equals(propertyId)
                        || "developerStatus".equals(propertyId);
            }
        });
    }

    private void buildGrid() {
        grid = new Grid();
        grid.setSizeFull();

        BeanItemContainer<VulnerabilityDTO> cont = new BeanItemContainer<>(
                VulnerabilityDTO.class);
        grid.setContainerDataSource(cont);
        grid.removeAllColumns();
        grid.addColumn("identifier");
        grid.addColumn("dependency");
        grid.addColumn("severityLevel");
        grid.addColumn("riskScore");
        grid.addColumn("vaadinAnalysis");
        grid.addColumn("developerStatus");
        grid.getColumn("identifier")
                .setHeaderCaption("Vulnerability name or identifier");
        grid.getColumn("severityLevel").setHeaderCaption("Severity");
        grid.getColumn("riskScore").setHeaderCaption("Risk score");
        grid.getColumn("vaadinAnalysis").setHeaderCaption("Vaadin analysis");
        grid.getColumn("developerStatus")
                .setHeaderCaption("Developer analysis");

        getMainContent().addComponent(grid);
        getMainContent().setExpandRatio(grid, 1);

        grid.addItemClickListener(event -> {
            if (event.isDoubleClick()) {
                showVulnerabilityDetails((VulnerabilityDTO) event.getItemId());
            }
        });

        Button showDetails = new Button("Show details");
        showDetails.setEnabled(false);
        getMainContent().addComponent(showDetails);
        getMainContent().setComponentAlignment(showDetails,
                Alignment.BOTTOM_RIGHT);
        showDetails.addClickListener(e -> showVulnerabilityDetails(
                (VulnerabilityDTO) grid.getSelectedRows().iterator().next()));
        grid.addSelectionListener(
                e -> showDetails.setEnabled(e.getSelected().size() != 0));
    }

    @SuppressWarnings("unchecked")
    private BeanItemContainer<VulnerabilityDTO> getContainer() {
        return (BeanItemContainer<VulnerabilityDTO>) grid
                .getContainerDataSource();
    }

    @Override
    public void refresh() {
        BeanItemContainer<VulnerabilityDTO> container = new BeanItemContainer<>(
                VulnerabilityDTO.class);
        AppSecService.getInstance().getVulnerabilities()
                .forEach(container::addBean);
        grid.setContainerDataSource(container);

        BeanItemContainer<DependencyDTO> depsCont = new BeanItemContainer<>(
                DependencyDTO.class);
        getContainer().getItemIds().stream()
                .map(VulnerabilityDTO::getDependency)
                .collect(Collectors.toSet()).forEach(depsCont::addBean);
        dependency.setContainerDataSource(depsCont);
    }

    /**
     * Filters the Vulnerability list using the given item.
     *
     * @param item
     *            filter
     */
    public void filterOn(DependencyDTO item) {
        clearFilters();
        dependency.setValue(item);
        applyFilters();
    }

    private void showVulnerabilityDetails(VulnerabilityDTO vulnerabilityDTO) {
        parent.showDetails(
                new VulnerabilityDetailsView(vulnerabilityDTO, () -> {
                    parent.showMainContent();
                    refresh();
                }));
    }
}
