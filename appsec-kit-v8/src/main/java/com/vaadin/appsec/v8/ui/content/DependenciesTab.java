package com.vaadin.appsec.v8.ui.content;

import java.util.stream.Collectors;

import com.vaadin.appsec.v8.data.DependencyDTO;
import com.vaadin.appsec.v8.data.SeverityLevel;
import com.vaadin.appsec.v8.service.AppSecDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

public class DependenciesTab extends AbstractAppSecContent {
    private Grid<DependencyDTO> grid;
    private ComboBox<String> group;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<Integer> riskScore;

    public DependenciesTab() {
        buildFilters();
        buildGrid();
    }

    private void buildFilters() {
        group = new ComboBox<>("Dependency group");

        severity = new ComboBox<>("Severity level");
        severity.setItems(SeverityLevel.NA, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH);

        riskScore = new ComboBox<>("Risk score");
        riskScore.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        buildFilterBar(group, severity/* ,riskScore */);
    }

    protected void clearFilters() {
        group.setValue(null);
        severity.setValue(null);
        riskScore.setValue(null);
        getListDataProvider().clearFilters();
    }

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
        grid.setSizeFull();
        grid.addColumn(DependencyDTO::getName).setCaption("Dependency");
        grid.addColumn(DependencyDTO::getNumOfVulnerabilities)
                .setCaption("# of vulnerabilities");
        grid.addColumn(DependencyDTO::getGroup).setCaption("Dependency group");
        grid.addColumn(DependencyDTO::getVersion).setCaption("Version");
        grid.addColumn(DependencyDTO::getSeverityLevel).setCaption("Severity");
        grid.addColumn(DependencyDTO::getRiskScore).setCaption("Risk score");

        addComponentsAndExpand(grid);

        grid.addItemClickListener(item -> {
            // TODO Open details view for clicked dependency
        });
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<DependencyDTO> getListDataProvider() {
        return (ListDataProvider<DependencyDTO>) grid.getDataProvider();
    }

    public void refresh() {
        grid.setItems(AppSecDataProvider.getDependencies());
        group.setItems(getListDataProvider().getItems().stream()
                .map(DependencyDTO::getGroup).collect(Collectors.toSet()));
    }
}
