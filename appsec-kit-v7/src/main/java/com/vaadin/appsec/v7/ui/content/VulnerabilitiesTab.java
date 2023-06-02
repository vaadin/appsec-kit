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

import com.vaadin.appsec.v7.data.DependencyDTO;
import com.vaadin.appsec.v7.data.SeverityLevel;
import com.vaadin.appsec.v7.data.VulnerabilityDTO;
import com.vaadin.appsec.v7.service.AppSecDataProvider;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
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

    /**
     * Instantiates a new Vulnerabilities tab.
     */
    public VulnerabilitiesTab() {
        buildFilters();
        buildGrid();
    }

    private void buildFilters() {
        dependency = new ComboBox("Dependency");

        vaadinAnalysis = new ComboBox("Vaadin analysis");
        // TODO Set vaadin analysis options

        devAnalysis = new ComboBox("Developer analysis");
        // TODO Set dev analysis options

        severity = new ComboBox("Severity level");
        severity.setContainerDataSource(buildSeverityContainer());

        buildFilterBar(dependency, vaadinAnalysis, devAnalysis, severity);
    }

    protected void clearFilters() {
        dependency.setValue(null);
        vaadinAnalysis.setValue(null);
        devAnalysis.setValue(null);
        severity.setValue(null);
        getContainer().removeAllContainerFilters();
    }

    protected void applyFilters() {
        getContainer().removeAllContainerFilters();

        final DependencyDTO dependencyFilter = (DependencyDTO) dependency
                .getValue();
        final SeverityLevel severityFilter = (SeverityLevel) severity
                .getValue();

        // TODO Add filtering for vaadin analysis
        // TODO Add filtering for developer analysis

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
                return true;
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return "dependency".equals(propertyId)
                        || "severityLevel".equals(propertyId);
            }
        });
    }

    private void buildGrid() {
        grid = new Grid();
        grid.setSizeFull();

        BeanItemContainer<VulnerabilityDTO> cont = new BeanItemContainer<>(
                VulnerabilityDTO.class);
        grid.setContainerDataSource(cont);

        grid.setColumns("identifier", "dependency", "severityLevel",
                "riskScore", "vaadinAnalysis", "developerAnalysis");
        grid.getColumn("identifier")
                .setHeaderCaption("Vulnerability name or identifier");
        grid.getColumn("severityLevel").setHeaderCaption("Severity");
        grid.getColumn("riskScore").setHeaderCaption("Risk score");
        grid.getColumn("vaadinAnalysis").setHeaderCaption("Vaadin analysis");
        grid.getColumn("developerAnalysis")
                .setHeaderCaption("Developer analysis");

        addComponent(grid);
        setExpandRatio(grid, 1);

        grid.addItemClickListener(item -> {
            // TODO Open details view for clicked vulnerability
        });
    }

    @SuppressWarnings("unchecked")
    private BeanItemContainer<VulnerabilityDTO> getContainer() {
        return (BeanItemContainer<VulnerabilityDTO>) grid
                .getContainerDataSource();
    }

    public void refresh() {
        getContainer().removeAllItems();
        AppSecDataProvider.getVulnerabilities()
                .forEach(vln -> getContainer().addBean(vln));

        BeanItemContainer<DependencyDTO> depsCont = new BeanItemContainer<>(
                DependencyDTO.class);
        getContainer().getItemIds().stream()
                .map(VulnerabilityDTO::getDependency)
                .collect(Collectors.toSet()).forEach(depsCont::addBean);
        dependency.setContainerDataSource(depsCont);
    }
}
