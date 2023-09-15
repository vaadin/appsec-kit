/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.v24.views;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

public class DependenciesTab extends AbstractAppSecView {

    private Grid<Dependency> grid;
    private GridListDataView<Dependency> dataView;
    private TextField searchField;
    private ComboBox<Ecosystem> ecosystem;
    private ComboBox<String> group;
    private ComboBox<Boolean> isDevelopment;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<String> riskScore;
    private final boolean includeNpmDevDeps;
    private final AppSecView parent;

    public DependenciesTab(AppSecView parent) {
        this.parent = parent;
        this.includeNpmDevDeps = AppSecService.getInstance().getConfiguration()
                .isIncludeNpmDevDependencies();
        buildFilters();
        buildGrid();
        configureSearchField();
    }

    @Override
    protected void clearFilters() {
        searchField.setValue("");
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
        dataView = grid.setItems(AppSecService.getInstance().getDependencies());
        dataView.addFilter(dependency -> {
            String searchTerm = searchField.getValue().trim();
            if (searchTerm.isEmpty()) {
                return true;
            }
            return dependency.getName().contains(searchTerm);
        });

        List<String> sortedGroups = getListDataProvider().getItems().stream()
                .map(Dependency::getGroup).filter(Objects::nonNull).distinct()
                .sorted().toList();
        group.setItems(sortedGroups);
        applyFilters();
    }

    private Double getRiskScoreFromFilter(String riskScoreFilter) {
        String[] parts = riskScoreFilter.split("=");
        return Double.valueOf(parts[1]);
    }

    private void configureSearchField() {
        dataView = grid.setItems(AppSecService.getInstance().getDependencies());

        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
    }

    private void buildFilters() {
        searchField = new TextField("Dependency name");

        ecosystem = new ComboBox<>("Ecosystem");
        ecosystem.setItems(Ecosystem.MAVEN, Ecosystem.NPM);

        group = new ComboBox<>("Dependency group");

        if (includeNpmDevDeps) {
            isDevelopment = new ComboBox<>("Is development?");
            isDevelopment.setItems(Boolean.TRUE, Boolean.FALSE);
        }

        severity = new ComboBox<>("Severity");
        severity.setItems(SeverityLevel.NONE, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH,
                SeverityLevel.CRITICAL);

        riskScore = new ComboBox<>("CVSS score");
        riskScore.setItems(">= 0", ">=1", ">=2", ">=3", ">=4", ">=5", ">=6",
                ">=7", ">=8", ">=9", ">=10");

        List<Component> components = Stream
                .of(searchField, ecosystem, group, severity, riskScore)
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
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setColumnReorderingAllowed(true);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSizeFull();

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
                .setResizable(true).setSortable(true)
                .setTooltipGenerator(Dependency::getCvssString);

        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                parent.showVulnerabilitiesTabFor(e.getItem());
            }
        });

        getMainContent().addAndExpand(grid);
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<Dependency> getListDataProvider() {
        return (ListDataProvider<Dependency>) grid.getDataProvider();
    }
}
