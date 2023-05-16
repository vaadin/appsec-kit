package com.vaadin.appsec.v8.ui.content;

import java.util.stream.Collectors;

import com.vaadin.appsec.v8.data.DependencyDTO;
import com.vaadin.appsec.v8.data.SeverityLevel;
import com.vaadin.appsec.v8.data.VulnerabilityDTO;
import com.vaadin.appsec.v8.service.AppSecDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

public class VulnerabilitiesTab extends AbstractAppSecContent {
    private Grid<VulnerabilityDTO> grid;
    private ComboBox<DependencyDTO> dependency;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<String> vaadinAnalysis;
    private ComboBox<String> devAnalysis;

    public VulnerabilitiesTab() {
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

        addComponentsAndExpand(grid);

        grid.addItemClickListener(item -> {
            // TODO Open details view for clicked vulnerability
        });
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<VulnerabilityDTO> getListDataProvider() {
        return (ListDataProvider<VulnerabilityDTO>) grid.getDataProvider();
    }

    public void refresh() {
        grid.setItems(AppSecDataProvider.getVulnerabilities());
        dependency.setItems(getListDataProvider().getItems().stream()
                .map(VulnerabilityDTO::getDependency)
                .collect(Collectors.toSet()));
        // TODO Update vaadin analysis options
        // TODO Update dev analysis options
    }
}
