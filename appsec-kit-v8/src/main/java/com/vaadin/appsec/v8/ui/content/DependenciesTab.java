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

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

/**
 * Dependencies tab content
 */
public class DependenciesTab extends AbstractAppSecContent {
    private Grid<Dependency> grid;
    private ComboBox<String> group;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<Integer> riskScore;
    private MainView parent;

    /**
     * Instantiates a new Dependencies tab.
     */
    public DependenciesTab(MainView parent) {
        this.parent = parent;
        buildFilters();
        buildGrid();
    }

    private void buildFilters() {
        group = new ComboBox<>("Dependency group");

        severity = new ComboBox<>("Severity level");
        severity.setItems(SeverityLevel.NONE, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH,
                SeverityLevel.CRITICAL);

        riskScore = new ComboBox<>("Risk score");
        riskScore.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        buildFilterBar(group, severity/* ,riskScore */);
    }

    @Override
    protected void clearFilters() {
        group.setValue(null);
        severity.setValue(null);
        riskScore.setValue(null);
        getListDataProvider().clearFilters();
    }

    @Override
    protected void applyFilters() {
        String groupFilter = group.getValue();
        SeverityLevel severityFilter = severity.getValue();
        Integer riskScoreFilter = riskScore.getValue();

        getListDataProvider().setFilter(dependencyDTO -> {
            if (groupFilter != null
                    && !groupFilter.equals(dependencyDTO.getGroup())) {
                return false;
            }
            if (severityFilter != null && !severityFilter
                    .equals(dependencyDTO.getSeverityLevel())) {
                return false;
            }
            if (riskScoreFilter != null
                    && !riskScoreFilter.equals(dependencyDTO.getRiskScore())) {
                return false;
            }
            return true;
        });
    }

    private void buildGrid() {
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.addColumn(Dependency::getName).setCaption("Dependency");
        grid.addColumn(Dependency::getNumOfVulnerabilities)
                .setCaption("# of vulnerabilities");
        grid.addColumn(Dependency::getGroup).setCaption("Dependency group");
        grid.addColumn(Dependency::getVersion).setCaption("Version");
        grid.addColumn(Dependency::getSeverityLevel).setCaption("Severity");
        grid.addColumn(Dependency::getRiskScore).setCaption("Risk score");

        getMainContent().addComponentsAndExpand(grid);

        grid.addItemClickListener(e -> {
            if (e.getMouseEventDetails().isDoubleClick()) {
                parent.showVulnerabilitiesTabFor(e.getItem());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<Dependency> getListDataProvider() {
        return (ListDataProvider<Dependency>) grid.getDataProvider();
    }

    @Override
    public void refresh() {
        grid.setItems(AppSecService.getInstance().getDependencies());
        group.setItems(getListDataProvider().getItems().stream()
                .map(Dependency::getGroup).collect(Collectors.toSet()));
        applyFilters();
    }
}
