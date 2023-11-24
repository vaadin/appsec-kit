/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.views;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

/**
 * Dependencies tab view contains a detailed list of dependencies.
 */
public class DependenciesView extends AbstractAppSecView {

    private Grid<Dependency> grid;
    private ComboBox<Ecosystem> ecosystem;
    private ComboBox<String> group;
    private ComboBox<Boolean> isDevelopment;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<String> riskScore;
    private final boolean includeNpmDevDeps;
    private final AppSecView parent;

    public DependenciesView(AppSecView parent) {
        this.parent = parent;
        this.includeNpmDevDeps = AppSecService.getInstance().getConfiguration()
                .isIncludeNpmDevDependencies();
        buildFilters();
        buildGrid();
        buildShowVulnerabilitiesButton();
    }

    @Override
    protected void clearFilters() {
        ecosystem.setValue(null);
        group.setValue(null);
        if (includeNpmDevDeps) {
            isDevelopment.setValue(null);
        }
        severity.setValue(null);
        riskScore.setValue(null);
        getListDataProvider().clearFilters();
    }

    @Override
    protected void applyFilters() {
        Ecosystem ecosystemFilter = ecosystem.getValue();
        String groupFilter = group.getValue();
        Boolean isDevelopmentFilter = includeNpmDevDeps
                ? isDevelopment.getValue()
                : null;
        SeverityLevel severityFilter = severity.getValue();
        Double riskScoreFilter = riskScore.getValue() != null
                ? getRiskScoreFromFilter(riskScore.getValue())
                : null;

        getListDataProvider().setFilter(dependencyDTO -> {
            if (ecosystemFilter != null
                    && !ecosystemFilter.equals(dependencyDTO.getEcosystem())) {
                return false;
            }
            if (groupFilter != null
                    && !groupFilter.equals(dependencyDTO.getGroup())) {
                return false;
            }
            if (includeNpmDevDeps && isDevelopmentFilter != null
                    && !isDevelopmentFilter == dependencyDTO
                            .isDevDependency()) {
                return false;
            }
            if (severityFilter != null && !severityFilter
                    .equals(dependencyDTO.getSeverityLevel())) {
                return false;
            }
            return riskScoreFilter == null
                    || riskScoreFilter <= dependencyDTO.getRiskScore();
        });
    }

    @Override
    public void refresh() {
        Set<Dependency> selectedItems = grid.getSelectedItems();
        grid.deselectAll();
        grid.setDataProvider(getDependencyDataProvider());

        group.setDataProvider(getGroupDataProvider());
        applyFilters();
        selectedItems.forEach(grid::select);
    }

    private Double getRiskScoreFromFilter(String riskScoreFilter) {
        String[] parts = riskScoreFilter.split("=");
        return Double.valueOf(parts[1]);
    }

    private void buildFilters() {
        ecosystem = new ComboBox<>("Ecosystem");
        ecosystem.setDataProvider(getEcosystemDataProvider());
        ecosystem.addValueChangeListener(event -> applyFilters());

        group = new ComboBox<>("Dependency group");
        group.addValueChangeListener(event -> applyFilters());
        group.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

        if (includeNpmDevDeps) {
            isDevelopment = new ComboBox<>("Is development?");
            isDevelopment.setDataProvider(getIsDevelopmentDataProvider());
            isDevelopment.addValueChangeListener(event -> applyFilters());
        }

        severity = new ComboBox<>("Severity");
        severity.setDataProvider(getSeverityLevelDataProvider());
        severity.addValueChangeListener(event -> applyFilters());

        riskScore = new ComboBox<>("CVSS score");
        riskScore.setDataProvider(getRiskScoreDataProvider());
        riskScore.addValueChangeListener(event -> applyFilters());

        List<Component> components = Stream
                .of(ecosystem, group, severity, riskScore)
                .collect(Collectors.toList());
        if (includeNpmDevDeps) {
            components.add(3, isDevelopment);
        }
        Component filterBar = buildFilterBar(
                components.toArray(Component[]::new));
        getMainContent().add(filterBar);
    }

    private void buildGrid() {
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setMultiSort(true);
        grid.setSizeFull();

        List<Dependency> dependencies = AppSecService.getInstance()
                .getDependencies();
        ListDataProvider<Dependency> dataProvider = new ListDataProvider<>(
                dependencies);
        grid.setDataProvider(dataProvider);

        grid.addColumn(Dependency::getName).setHeader("Dependency")
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getEcosystem).setHeader("Ecosystem")
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getGroup).setHeader("Dependency group")
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getVersion).setHeader("Version")
                .setResizable(true).setSortable(true);
        if (includeNpmDevDeps) {
            grid.addColumn(Dependency::isDevDependency)
                    .setHeader("Is development?").setResizable(true)
                    .setSortable(true);
        }
        grid.addColumn(Dependency::getNumOfVulnerabilities)
                .setHeader("# of vulnerabilities").setResizable(true)
                .setSortable(true);
        grid.addColumn(Dependency::getSeverityLevel)
                .setHeader("Highest severity").setResizable(true)
                .setSortable(true);
        grid.addColumn(Dependency::getRiskScore).setHeader("Highest CVSS score")
                .setResizable(true).setSortable(true);

        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                parent.showVulnerabilitiesTabFor(e.getItem());
            }
        });

        getMainContent().addAndExpand(grid);
    }

    private void buildShowVulnerabilitiesButton() {
        Button showVulnerabilities = new Button("Show vulnerabilities");
        showVulnerabilities.setEnabled(false);
        showVulnerabilities.getElement().setAttribute("aria-label",
                "Show vulnerabilities");
        showVulnerabilities
                .addClickListener(e -> parent.showVulnerabilitiesTabFor(
                        grid.getSelectedItems().iterator().next()));
        grid.addSelectionListener(e -> showVulnerabilities
                .setEnabled(e.getFirstSelectedItem().isPresent()));

        getMainContent().add(showVulnerabilities);
        getMainContent().setHorizontalComponentAlignment(Alignment.END,
                showVulnerabilities);
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<Dependency> getListDataProvider() {
        return (ListDataProvider<Dependency>) grid.getDataProvider();
    }

    private ListDataProvider<Dependency> getDependencyDataProvider() {
        return new ListDataProvider<>(
                AppSecService.getInstance().getDependencies());
    }

    private ListDataProvider<String> getGroupDataProvider() {
        return new ListDataProvider<>(getDependencyDataProvider().getItems()
                .stream().map(Dependency::getGroup).filter(Objects::nonNull)
                .distinct().sorted().collect(Collectors.toList()));
    }

    private ListDataProvider<Ecosystem> getEcosystemDataProvider() {
        return new ListDataProvider<>(Arrays.asList(Ecosystem.values()));
    }

    private ListDataProvider<Boolean> getIsDevelopmentDataProvider() {
        return new ListDataProvider<>(
                Arrays.asList(Boolean.TRUE, Boolean.FALSE));
    }

    private ListDataProvider<SeverityLevel> getSeverityLevelDataProvider() {
        return new ListDataProvider<>(Arrays.asList(SeverityLevel.values()));
    }

    private ListDataProvider<String> getRiskScoreDataProvider() {
        return new ListDataProvider<>(Arrays.asList(">=0", ">=1", ">=2", ">=3",
                ">=4", ">=5", ">=6", ">=7", ">=8", ">=9", "=10"));
    }
}
