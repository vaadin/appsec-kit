/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;

/**
 * Dependencies view contains a detailed list of dependencies.
 */
public class DependenciesView extends AbstractAppSecView {

    private final Logger logger = LoggerFactory
            .getLogger(DependenciesView.class);

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

    public DependenciesView(AppSecView parent) {
        this.parent = parent;
        this.includeNpmDevDeps = AppSecService.getInstance().getConfiguration()
                .isIncludeNpmDevDependencies();
        buildFilters();
        buildGrid();
        buildShowVulnerabilitiesButton();
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
        Set<Dependency> selectedItems = grid.getSelectedItems();
        grid.deselectAll();

        List<Dependency> dependencies = AppSecService.getInstance()
                .getDependencies();
        dataView = grid.setItems(dependencies);
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
        selectedItems.forEach(grid::select);

        prepareExportData(dependencies);
    }

    private void prepareExportData(List<Dependency> dependencyList) {
        exportLink.setEnabled(false); // disable while preparing data
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CSVPrinter printer = new CSVPrinter(
                        new OutputStreamWriter(outputStream),
                        CSVFormat.DEFAULT)) {
            // header
            printer.printRecord(DEPENDENCY, ECOSYSTEM, DEPENDENCY_GROUP,
                    VERSION, IS_DEVELOPMENT, NUMBER_OF_VULNERABILITIES,
                    HIGHEST_SEVERITY, HIGHEST_CVSS_SCORE);
            // content
            for (Dependency dependency : dependencyList) {
                printer.printRecord(dependency.getName(),
                        dependency.getEcosystem(), dependency.getGroup(),
                        dependency.getVersion(), dependency.isDevDependency(),
                        dependency.getNumOfVulnerabilities(),
                        dependency.getSeverityLevel(),
                        dependency.getRiskScore());
            }

            String fileName = "dependencies.csv";
            StreamResource streamResource = new StreamResource(fileName,
                    () -> new ByteArrayInputStream(outputStream.toByteArray()));
            updateExportData(streamResource);
            // enable now that there is data to download
            exportLink.setEnabled(true);
        } catch (IOException e) {
            logger.error("Error preparing export data", e);
            Notification errorNotification = new Notification(
                    "Data cannot be exported due to an error.", 5000,
                    Notification.Position.TOP_END);
            errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorNotification.open();
        }
    }

    private Double getRiskScoreFromFilter(String riskScoreFilter) {
        String[] parts = riskScoreFilter.split("=");
        return Double.valueOf(parts[1]);
    }

    private void configureSearchField() {
        dataView = grid.setItems(AppSecService.getInstance().getDependencies());

        searchField.setPlaceholder(SEARCH);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
    }

    private void buildFilters() {
        searchField = new TextField(DEPENDENCY_NAME);

        ecosystem = new ComboBox<>(ECOSYSTEM);
        ecosystem.setItems(Ecosystem.MAVEN, Ecosystem.NPM);
        ecosystem.addValueChangeListener(event -> applyFilters());

        group = new ComboBox<>(DEPENDENCY_GROUP);
        group.addValueChangeListener(event -> applyFilters());
        group.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

        if (includeNpmDevDeps) {
            isDevelopment = new ComboBox<>(IS_DEVELOPMENT);
            isDevelopment.setItems(Boolean.TRUE, Boolean.FALSE);
            isDevelopment.addValueChangeListener(event -> applyFilters());
        }

        severity = new ComboBox<>(SEVERITY);
        severity.setItems(SeverityLevel.NONE, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH,
                SeverityLevel.CRITICAL);
        severity.addValueChangeListener(event -> applyFilters());

        riskScore = new ComboBox<>(CVSS_SCORE);
        riskScore.setItems(">=0", ">=1", ">=2", ">=3", ">=4", ">=5", ">=6",
                ">=7", ">=8", ">=9", "=10");
        riskScore.addValueChangeListener(event -> applyFilters());

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
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSizeFull();

        grid.addColumn(Dependency::getName).setHeader(DEPENDENCY)
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getEcosystem).setHeader(ECOSYSTEM)
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getGroup).setHeader(DEPENDENCY_GROUP)
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getVersion).setHeader(VERSION)
                .setResizable(true).setSortable(true);
        if (includeNpmDevDeps) {
            grid.addColumn(Dependency::isDevDependency)
                    .setHeader(IS_DEVELOPMENT).setResizable(true)
                    .setSortable(true);
        }
        grid.addColumn(Dependency::getNumOfVulnerabilities)
                .setHeader(NUMBER_OF_VULNERABILITIES).setResizable(true)
                .setSortable(true);
        grid.addColumn(Dependency::getSeverityLevel).setHeader(HIGHEST_SEVERITY)
                .setResizable(true).setSortable(true);
        grid.addColumn(Dependency::getRiskScore).setHeader(HIGHEST_CVSS_SCORE)
                .setResizable(true).setSortable(true)
                .setTooltipGenerator(Dependency::getCvssString);

        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                parent.showVulnerabilitiesViewFor(e.getItem());
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
                .addClickListener(e -> parent.showVulnerabilitiesViewFor(
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
}
