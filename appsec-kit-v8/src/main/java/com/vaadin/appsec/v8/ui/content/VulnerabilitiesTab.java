/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui.content;

import java.util.stream.Collectors;

import com.vaadin.appsec.v8.data.DependencyDTO;
import com.vaadin.appsec.v8.data.SeverityLevel;
import com.vaadin.appsec.v8.data.VulnerabilityDTO;
import com.vaadin.appsec.v8.service.AppSecDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

/**
 * Vulnerabilities tab content
 */
public class VulnerabilitiesTab extends AbstractAppSecContent {
    private AbstractAppSecContent parent;
    private Grid<VulnerabilityDTO> grid;
    private ComboBox<DependencyDTO> dependency;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<String> vaadinAnalysis;
    private ComboBox<String> devAnalysis;

    /**
     * Instantiates a new Vulnerabilities tab.
     */
    public VulnerabilitiesTab(AbstractAppSecContent parent) {
        this.parent = parent;
        buildFilters();
        buildGrid();
    }

    private void buildFilters() {
        dependency = new ComboBox<>("Dependency");

        vaadinAnalysis = new ComboBox<>("Vaadin analysis");

        devAnalysis = new ComboBox<>("Developer analysis");

        severity = new ComboBox<>("Severity level");
        severity.setItems(SeverityLevel.NA, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH);

        buildFilterBar(dependency, vaadinAnalysis, devAnalysis, severity);
    }

    protected void clearFilters() {
        dependency.setValue(null);
        vaadinAnalysis.setValue(null);
        devAnalysis.setValue(null);
        severity.setValue(null);
        getListDataProvider().clearFilters();
    }

    protected void applyFilters() {
        DependencyDTO dependencyFilter = dependency.getValue();
        String vaadinAnalysisFilter = vaadinAnalysis.getValue();
        String devAnalysisFilter = devAnalysis.getValue();
        SeverityLevel severityFilter = severity.getValue();

        getListDataProvider().setFilter(vulnerabilityDTO -> {
            if (dependencyFilter != null && !dependencyFilter
                    .equals(vulnerabilityDTO.getDependency())) {
                return false;
            }
            if (vaadinAnalysisFilter != null && !vaadinAnalysisFilter
                    .equals(vulnerabilityDTO.getVaadinAnalysis())) {
                return false;
            }
            if (devAnalysisFilter != null && !devAnalysisFilter
                    .equals(vulnerabilityDTO.getDeveloperAnalysis())) {
                return false;
            }
            if (severityFilter != null && !severityFilter
                    .equals(vulnerabilityDTO.getSeverityLevel())) {
                return false;
            }
            return true;
        });
    }

    private void buildGrid() {
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();

        grid.addColumn(VulnerabilityDTO::getIdentifier)
                .setCaption("Vulnerability name or identifier");
        grid.addColumn(VulnerabilityDTO::getDependency)
                .setCaption("Dependency");
        grid.addColumn(VulnerabilityDTO::getSeverityLevel)
                .setCaption("Severity");
        grid.addColumn(VulnerabilityDTO::getRiskScore).setCaption("Risk score");
        grid.addColumn(VulnerabilityDTO::getVaadinAnalysis)
                .setCaption("Vaadin analysis");
        grid.addColumn(VulnerabilityDTO::getDeveloperAnalysis)
                .setCaption("Developer analysis");

        getMainContent().addComponentsAndExpand(grid);

        grid.addItemClickListener(e -> {
            if (e.getMouseEventDetails().isDoubleClick()) {
                parent.showDetails(
                        new VulnerabilityDetailsView(e.getItem(), () -> {
                            parent.showMainContent();
                            refresh();
                        }));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<VulnerabilityDTO> getListDataProvider() {
        return (ListDataProvider<VulnerabilityDTO>) grid.getDataProvider();
    }

    @Override
    public void refresh() {
        grid.setItems(AppSecDataProvider.getVulnerabilities());
        dependency.setItems(getListDataProvider().getItems().stream()
                .map(VulnerabilityDTO::getDependency)
                .collect(Collectors.toSet()));
        applyFilters();
        // TODO Update vaadin analysis options
        // TODO Update dev analysis options
    }

    void filterOn(DependencyDTO item) {
        clearFilters();
        dependency.setValue(item);
        applyFilters();
    }
}
