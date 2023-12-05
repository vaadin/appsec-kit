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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.StreamResource;

/**
 * Vulnerabilities view contains a detailed list of vulnerabilities.
 */
public class VulnerabilitiesView extends AbstractAppSecView {

    private final Logger logger = LoggerFactory
            .getLogger(VulnerabilitiesView.class);

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

    public VulnerabilitiesView(AbstractAppSecView parent) {
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
        List<Vulnerability> vulnerabilities = AppSecService.getInstance()
                .getVulnerabilities();
        grid.setItems(vulnerabilities);
        dependency.setItems(getListDataProvider().getItems().stream()
                .map(Vulnerability::getDependency).collect(Collectors.toSet()));
        applyFilters();
        selectedItems.forEach(grid::select);

        // prepare export data
        prepareExportData(vulnerabilities);
    }

    private void prepareExportData(List<Vulnerability> vulnerabilityList) {
        exportLink.setEnabled(false); // disable while preparing data
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CSVPrinter printer = new CSVPrinter(
                        new OutputStreamWriter(outputStream),
                        CSVFormat.DEFAULT)) {
            // header
            printer.printRecord(VULNERABILITY_NAME_OR_IDENTIFIER, ECOSYSTEM,
                    DEPENDENCY, SEVERITY, CVSS_SCORE, VAADIN_ANALYSIS,
                    DEVELOPER_ANALYSIS);
            // content
            for (Vulnerability vulnerability : vulnerabilityList) {
                printer.printRecord(vulnerability.getIdentifier(),
                        ecosystemValueProvider.apply(vulnerability),
                        vulnerability.getDependency(),
                        vulnerability.getSeverityLevel(),
                        vulnerability.getRiskScore(),
                        vulnerability.getVaadinAnalysis(),
                        vulnerability.getDeveloperAnalysis());
            }

            String fileName = "vulnerabilities.csv";
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

    private void buildFilters() {
        ecosystem = new ComboBox<>(ECOSYSTEM);
        ecosystem.setItems(Ecosystem.MAVEN, Ecosystem.NPM);
        ecosystem.addValueChangeListener(event -> applyFilters());

        dependency = new ComboBox<>(DEPENDENCY);
        dependency.addValueChangeListener(event -> applyFilters());
        dependency.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

        vaadinAnalysis = new ComboBox<>(VAADIN_ANALYSIS);
        vaadinAnalysis.setItems(AssessmentStatus.TRUE_POSITIVE,
                AssessmentStatus.FALSE_POSITIVE, AssessmentStatus.UNDER_REVIEW);
        vaadinAnalysis.addValueChangeListener(event -> applyFilters());

        developerAnalysis = new ComboBox<>(DEVELOPER_ANALYSIS);
        developerAnalysis.setItems(AppSecData.VulnerabilityStatus.NOT_SET,
                AppSecData.VulnerabilityStatus.NOT_AFFECTED,
                AppSecData.VulnerabilityStatus.FALSE_POSITIVE,
                AppSecData.VulnerabilityStatus.IN_TRIAGE,
                AppSecData.VulnerabilityStatus.EXPLOITABLE);
        developerAnalysis.addValueChangeListener(event -> applyFilters());

        severity = new ComboBox<>(SEVERITY);
        severity.setItems(SeverityLevel.NONE, SeverityLevel.LOW,
                SeverityLevel.MEDIUM, SeverityLevel.HIGH,
                SeverityLevel.CRITICAL);
        severity.addValueChangeListener(event -> applyFilters());

        riskScore = new ComboBox<>(CVSS_SCORE);
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
                .setHeader(VULNERABILITY_NAME_OR_IDENTIFIER).setResizable(true)
                .setSortable(true);
        grid.addColumn(ecosystemValueProvider).setHeader(ECOSYSTEM)
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getDependency).setHeader(DEPENDENCY)
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getSeverityLevel).setHeader(SEVERITY)
                .setResizable(true).setSortable(true);
        grid.addColumn(Vulnerability::getRiskScore).setHeader(CVSS_SCORE)
                .setResizable(true).setSortable(true)
                .setTooltipGenerator(Vulnerability::getCvssString);
        grid.addColumn(Vulnerability::getVaadinAnalysis)
                .setHeader(VAADIN_ANALYSIS).setResizable(true)
                .setSortable(true);
        grid.addColumn(Vulnerability::getDeveloperStatus)
                .setHeader(DEVELOPER_ANALYSIS).setResizable(true)
                .setSortable(true);

        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                showVulnerabilityDetails(e.getItem());
            }
        });

        getMainContent().addAndExpand(grid);
    }

    private void buildShowDetailsButton() {
        Button showDetails = new Button(SHOW_DETAILS);
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
