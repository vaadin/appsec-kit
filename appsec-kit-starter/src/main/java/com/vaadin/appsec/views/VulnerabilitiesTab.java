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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.analysis.AssessmentStatus;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.StreamResource;

/**
 * Vulnerabilities tab view contains a detailed list of vulnerabilities.
 */
public class VulnerabilitiesTab extends AbstractAppSecView {

    private Grid<Vulnerability> grid;
    private ComboBox<Ecosystem> ecosystem;
    private ComboBox<Dependency> dependency;
    private ComboBox<SeverityLevel> severity;
    private ComboBox<String> riskScore;
    private ComboBox<AssessmentStatus> vaadinAnalysis;
    private ComboBox<AppSecData.VulnerabilityStatus> developerAnalysis;
    private final AbstractAppSecView parent;
    private final ValueProvider<Vulnerability, Ecosystem> ecosystemValueProvider = vuln -> vuln
            .getDependency().getEcosystem();

    public VulnerabilitiesTab(AbstractAppSecView parent) {
        this.parent = parent;
        buildFilters();
        buildGrid();
        buildShowDetailsButton();
    }

    public void filterOn(Dependency item) {
        clearFilters();
        dependency.setValue(item);
        applyFilters();
    }

    @Override
    protected void clearFilters() {
        ecosystem.setValue(null);
        dependency.setValue(null);
        vaadinAnalysis.setValue(null);
        developerAnalysis.setValue(null);
        severity.setValue(null);
        riskScore.setValue(null);
        getListDataProvider().clearFilters();
    }

    @Override
    protected void applyFilters() {
        Ecosystem ecosystemFilter = ecosystem.getValue();
        Dependency dependencyFilter = dependency.getValue();
        AssessmentStatus vaadinAnalysisFilter = vaadinAnalysis.getValue();
        AppSecData.VulnerabilityStatus developerAnalysisFilter = developerAnalysis
                .getValue();
        SeverityLevel severityFilter = severity.getValue();
        Double riskScoreFilter = riskScore.getValue() != null
                ? getRiskScoreFromFilter(riskScore.getValue())
                : null;

        getListDataProvider().setFilter(vulnerabilityDTO -> {
            if (ecosystemFilter != null && !ecosystemFilter
                    .equals(vulnerabilityDTO.getDependency().getEcosystem())) {
                return false;
            }
            if (dependencyFilter != null && !dependencyFilter
                    .equals(vulnerabilityDTO.getDependency())) {
                return false;
            }
            if (vaadinAnalysisFilter != null && !vaadinAnalysisFilter
                    .equals(vulnerabilityDTO.getVaadinAnalysis())) {
                return false;
            }
            if (developerAnalysisFilter != null && !developerAnalysisFilter
                    .equals(vulnerabilityDTO.getDeveloperStatus())) {
                return false;
            }
            if (severityFilter != null && !severityFilter
                    .equals(vulnerabilityDTO.getSeverityLevel())) {
                return false;
            }
            return riskScoreFilter == null
                    || riskScoreFilter <= vulnerabilityDTO.getRiskScore();
        });
    }

    @Override
    public void refresh() {
        Set<Vulnerability> selectedItems = grid.getSelectedItems();
        grid.deselectAll();
        List<Vulnerability> vulnerabilities = AppSecService.getInstance().getVulnerabilities();
        grid.setItems(vulnerabilities);
        dependency.setItems(getListDataProvider().getItems().stream()
                .map(Vulnerability::getDependency).collect(Collectors.toSet()));
        applyFilters();
        selectedItems.forEach(grid::select);

        // prepare export data
        prepareExportData(vulnerabilities);
    }

    private void prepareExportData(List<Vulnerability> vulnerabilityList) {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)
        ) {
            // header
            printer.printRecord("Vulnerability name or identifier", "Ecosystem", "Dependency", "Severity", "CVSS score", "Vaadin analysis", "Developer analysis");
            // content
            for (Vulnerability vulnerability : vulnerabilityList) {
                printer.printRecord(
                        vulnerability.getIdentifier(),
                        ecosystemValueProvider.apply(vulnerability),
                        vulnerability.getDependency(),
                        vulnerability.getSeverityLevel(),
                        vulnerability.getRiskScore(),
                        vulnerability.getVaadinAnalysis(),
                        vulnerability.getDeveloperAnalysis()
                );
            }

            String fileName = "vulnerabilities.csv";
            StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(outputStream.toByteArray()));
            updateExportData(streamResource);
        } catch (IOException e) {
            // TODO handle exception properly
            e.printStackTrace();
        }
    }

    private Double getRiskScoreFromFilter(String riskScoreFilter) {
        String[] parts = riskScoreFilter.split("=");
        return Double.valueOf(parts[1]);
    }

    private void buildFilters() {
        ecosystem = new ComboBox<>("Ecosystem");
        ecosystem.setItems(Ecosystem.MAVEN, Ecosystem.NPM);
        ecosystem.addValueChangeListener(event -> applyFilters());

        dependency = new ComboBox<>("Dependency");
        dependency.addValueChangeListener(event -> applyFilters());
        dependency.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

        vaadinAnalysis = new ComboBox<>("Vaadin analysis");
        vaadinAnalysis.setItems(AssessmentStatus.TRUE_POSITIVE,
                AssessmentStatus.FALSE_POSITIVE, AssessmentStatus.UNDER_REVIEW);
        vaadinAnalysis.addValueChangeListener(event -> applyFilters());

        developerAnalysis = new ComboBox<>("Developer analysis");
        developerAnalysis.setItems(AppSecData.VulnerabilityStatus.NOT_SET,
                AppSecData.VulnerabilityStatus.NOT_AFFECTED,
                AppSecData.VulnerabilityStatus.FALSE_POSITIVE,
                AppSecData.VulnerabilityStatus.IN_TRIAGE,
                AppSecData.VulnerabilityStatus.EXPLOITABLE);
        developerAnalysis.addValueChangeListener(event -> applyFilters());

        severity = new ComboBox<>("Severity");
        severity.setItems(SeverityLevel.NONE, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH,
                SeverityLevel.CRITICAL);
        severity.addValueChangeListener(event -> applyFilters());

        riskScore = new ComboBox<>("CVSS score");
        riskScore.setItems(">=0", ">=1", ">=2", ">=3", ">=4", ">=5", ">=6",
                ">=7", ">=8", ">=9", "=10");
        riskScore.addValueChangeListener(event -> applyFilters());

        Component filterBar = buildFilterBar(ecosystem, dependency,
                vaadinAnalysis, developerAnalysis, severity, riskScore);
        getMainContent().add(filterBar);
    }

    private void buildGrid() {
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSizeFull();

        grid.addColumn(Vulnerability::getIdentifier)
                .setHeader("Vulnerability name or identifier")
                .setResizable(true).setSortable(true);
        grid.addColumn(ecosystemValueProvider).setHeader("Ecosystem")
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getDependency).setHeader("Dependency")
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getSeverityLevel).setHeader("Severity")
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getRiskScore).setHeader("CVSS score")
                .setResizable(true).setSortable(true)
                .setTooltipGenerator(Vulnerability::getCvssString);
        grid.addColumn(Vulnerability::getVaadinAnalysis)
                .setHeader("Vaadin analysis").setResizable(true)
                .setSortable(true);
        grid.addColumn(Vulnerability::getDeveloperStatus)
                .setHeader("Developer analysis").setResizable(true)
                .setSortable(true);

        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                showVulnerabilityDetails(e.getItem());
            }
        });

        getMainContent().addAndExpand(grid);
    }

    private void buildShowDetailsButton() {
        Button showDetails = new Button("Show details");
        showDetails.setEnabled(false);
        showDetails.addClickListener(e -> showVulnerabilityDetails(
                grid.getSelectedItems().iterator().next()));
        grid.addSelectionListener(e -> showDetails
                .setEnabled(e.getFirstSelectedItem().isPresent()));

        getMainContent().add(showDetails);
        getMainContent().setHorizontalComponentAlignment(Alignment.END,
                showDetails);
    }

    private void showVulnerabilityDetails(Vulnerability vulnerabilityDTO) {
        parent.showDetails(
                new VulnerabilityDetailsView(vulnerabilityDTO, () -> {
                    parent.showMainContent();
                    refresh();
                }));
    }

    @SuppressWarnings("unchecked")
    private ListDataProvider<Vulnerability> getListDataProvider() {
        return (ListDataProvider<Vulnerability>) grid.getDataProvider();
    }
}
