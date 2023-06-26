/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui.content;

import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.dto.DependencyDTO;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.dto.VulnerabilityDTO;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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
    private ComboBox<AppSecData.VulnerabilityStatus> devAnalysis;

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
        devAnalysis.setItems(AppSecData.VulnerabilityStatus.NOT_SET,
                AppSecData.VulnerabilityStatus.NOT_AFFECTED,
                AppSecData.VulnerabilityStatus.FALSE_POSITIVE,
                AppSecData.VulnerabilityStatus.IN_TRIAGE,
                AppSecData.VulnerabilityStatus.EXPLOITABLE);

        severity = new ComboBox<>("Severity level");
        severity.setItems(SeverityLevel.NA, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH);

        buildFilterBar(dependency, vaadinAnalysis, devAnalysis, severity);
    }

    @Override
    protected void clearFilters() {
        dependency.setValue(null);
        vaadinAnalysis.setValue(null);
        devAnalysis.setValue(null);
        severity.setValue(null);
        getListDataProvider().clearFilters();
    }

    @Override
    protected void applyFilters() {
        DependencyDTO dependencyFilter = dependency.getValue();
        String vaadinAnalysisFilter = vaadinAnalysis.getValue();
        AppSecData.VulnerabilityStatus devAnalysisFilter = devAnalysis
                .getValue();
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
                    .equals(vulnerabilityDTO.getDeveloperStatus())) {
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
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
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
        grid.addColumn(VulnerabilityDTO::getDeveloperStatus)
                .setCaption("Developer analysis");

        getMainContent().addComponentsAndExpand(grid);

        grid.addItemClickListener(e -> {
            if (e.getMouseEventDetails().isDoubleClick()) {
                showDetails(e.getItem());
            }
        });

        Button showDetails = new Button("Show details");
        showDetails.setEnabled(false);
        addComponent(showDetails);
        setComponentAlignment(showDetails, Alignment.BOTTOM_RIGHT);
        showDetails.addClickListener(
                e -> showDetails(grid.getSelectedItems().iterator().next()));
        grid.addSelectionListener(e -> showDetails
                .setEnabled(e.getFirstSelectedItem().isPresent()));
    }

    private void showDetails(VulnerabilityDTO item) {
        parent.showDetails(new VulnerabilityDetailsView(item, () -> {
            parent.showMainContent();
            refresh();
        }));
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<VulnerabilityDTO> getListDataProvider() {
        return (ListDataProvider<VulnerabilityDTO>) grid.getDataProvider();
    }

    @Override
    public void refresh() {
        Set<VulnerabilityDTO> selectedItems = grid.getSelectedItems();
        grid.deselectAll();
        grid.setItems(AppSecService.getInstance().getVulnerabilities());
        dependency.setItems(getListDataProvider().getItems().stream()
                .map(VulnerabilityDTO::getDependency)
                .collect(Collectors.toSet()));
        applyFilters();
        selectedItems.forEach(grid::select);

        // TODO Update vaadin analysis options
        // TODO Update dev analysis options
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
}
