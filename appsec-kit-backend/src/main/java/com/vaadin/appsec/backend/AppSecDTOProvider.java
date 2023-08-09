/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.cyclonedx.model.Component;
import us.springett.cvss.Cvss;

import com.vaadin.appsec.backend.model.AppSecData;
import com.vaadin.appsec.backend.model.analysis.AffectedVersion;
import com.vaadin.appsec.backend.model.analysis.Assessment;
import com.vaadin.appsec.backend.model.analysis.VulnerabilityDetails;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.dto.SeverityLevelComparator;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.appsec.backend.model.osv.response.Affected;
import com.vaadin.appsec.backend.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.backend.model.osv.response.Range;

/**
 * Helper class to provide bill of materials and vulnerabilities as DTOs for use
 * in the UI.
 */
class AppSecDTOProvider {

    private final VulnerabilityStore vulnerabilityStore;

    private final BillOfMaterialsStore bomStore;

    AppSecDTOProvider(VulnerabilityStore vulnerabilityStore,
            BillOfMaterialsStore bomStore) {
        this.vulnerabilityStore = vulnerabilityStore;
        this.bomStore = bomStore;
    }

    List<Dependency> getDependencies() {
        final List<OpenSourceVulnerability> vulnerabilities = vulnerabilityStore
                .getVulnerabilities();
        final List<org.cyclonedx.model.Dependency> dependencies = bomStore
                .getBom().getDependencies();

        return bomStore.getBom().getComponents().stream().map(component -> {
            Dependency dependency = new Dependency(component.getGroup(),
                    component.getName(), component.getVersion());
            Optional<org.cyclonedx.model.Dependency> parentDep = dependencies
                    .stream()
                    .filter(dep -> dep.getDependencies().stream()
                            .anyMatch(transDep -> transDep.getRef()
                                    .equals(component.getBomRef())))
                    .findFirst();
            parentDep.ifPresent(
                    parent -> dependency.setParentBomRef(parent.getRef()));
            updateVulnerabilityStatistics(dependency, vulnerabilities,
                    getConcatDepName(component));
            return dependency;
        }).collect(Collectors.toList());
    }

    List<Vulnerability> getVulnerabilities() {
        final List<Dependency> dependencies = getDependencies();
        final List<OpenSourceVulnerability> vulnerabilities = vulnerabilityStore
                .getVulnerabilities();
        final Map<String, AppSecData.VulnerabilityAssessment> devAnalysis = AppSecService
                .getInstance().getData().getVulnerabilities();

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        List<Vulnerability> vulnerabilityDTOS = new ArrayList<>();
        for (OpenSourceVulnerability v : vulnerabilities) {
            for (Affected affected : v.getAffected()) {
                String depGroup = getDepGroup(affected);
                String depName = getDepName(affected);

                Dependency dependencyDTO = dependencies.stream()
                        .filter(d -> d.getGroup().equals(depGroup)
                                && d.getName().equals(depName)
                                && affected.getVersions()
                                        .contains(d.getVersion()))
                        .findFirst().orElse(null);
                if (dependencyDTO != null) {
                    String id = getVulnerabilityId(v);
                    Vulnerability vulnerabilityDTO = new Vulnerability(id);
                    vulnerabilityDTO.setDependency(dependencyDTO);
                    vulnerabilityDTO.setDatePublished(v.getPublished());

                    String patchedVersion = getPatchedVersion(affected)
                            .orElse("---");
                    vulnerabilityDTO.setPatchedVersion(patchedVersion);

                    if (v.getDetails() != null) {
                        Node document = parser.parse(v.getDetails());
                        vulnerabilityDTO.setDetails(renderer.render(document));
                    }

                    Optional<AffectedVersion> vaadinAnalysis = getVaadinAnalysis(
                            vulnerabilityDTO);
                    vaadinAnalysis.ifPresent(affectedVersion -> vulnerabilityDTO
                            .setVaadinAnalysis(affectedVersion.getStatus()));

                    AppSecData.VulnerabilityAssessment vulnDevAnalysis = devAnalysis
                            .get(id);
                    if (vulnDevAnalysis != null) {
                        vulnerabilityDTO.setDeveloperStatus(
                                vulnDevAnalysis.getStatus());
                        vulnerabilityDTO.setDeveloperAnalysis(
                                vulnDevAnalysis.getDeveloperAnalysis());
                        vulnerabilityDTO.setDeveloperUpdated(
                                vulnDevAnalysis.getUpdated());
                    }

                    Set<String> urls = new HashSet<>();
                    v.getReferences().forEach(ref -> {
                        urls.add(ref.getUrl().toString());
                    });
                    vulnerabilityDTO.setReferenceUrls(urls);

                    vulnerabilityDTOS.add(vulnerabilityDTO);
                }
            }
        }
        return vulnerabilityDTOS;
    }

    private static void updateVulnerabilityStatistics(Dependency dependency,
            List<OpenSourceVulnerability> vulnerabilities,
            String concatDepName) {
        int vulnerabilityCount = 0;
        SeverityLevel highestSeverityLevel = SeverityLevel.NA;
        Double highestScore = 0.0;

        for (OpenSourceVulnerability vulnerability : vulnerabilities) {
            for (Affected affected : vulnerability.getAffected()) {
                if (concatDepName.equals(affected.getPackage().getName())) {
                    vulnerabilityCount++;
                    highestSeverityLevel = findSeverityIfHigher(vulnerability,
                            highestSeverityLevel);
                    highestScore = findScoreIfHigher(vulnerability,
                            highestScore);
                }
            }
        }

        dependency.setNumOfVulnerabilities(vulnerabilityCount);
        dependency.setSeverityLevel(highestSeverityLevel);
        dependency.setRiskScore(highestScore);
    }

    private static SeverityLevel findSeverityIfHigher(
            OpenSourceVulnerability vulnerability,
            SeverityLevel highestSeverityLevel) {
        String severity = String.valueOf(vulnerability.databaseSpecific()
                .getAdditionalProperties().get("severity"));
        SeverityLevel severityLevel = Arrays.stream(SeverityLevel.values())
                .filter(sl -> sl.name().equalsIgnoreCase(severity)).findAny()
                .orElse(SeverityLevel.NA);
        return SeverityLevelComparator.compareStatic(severityLevel,
                highestSeverityLevel) > 0 ? severityLevel
                        : highestSeverityLevel;
    }

    private static Double findScoreIfHigher(
            OpenSourceVulnerability vulnerability, Double highestScore) {
        Double hiScoreInVuln = vulnerability.getSeverity().stream()
                .map(severity -> Cvss.fromVector(severity.getScore()))
                .map(cvss -> cvss.calculateScore().getBaseScore())
                .max(Comparator.naturalOrder()).orElse(0.0);
        return hiScoreInVuln > highestScore ? hiScoreInVuln : highestScore;
    }

    private static String getConcatDepName(Component c) {
        return c.getGroup() + ":" + c.getName();
    }

    private static String getDepGroup(Affected affected) {
        return affected.getPackage().getName().split(":")[0];
    }

    private static String getDepName(Affected affected) {
        return affected.getPackage().getName().split(":")[1];
    }

    private static Optional<String> getPatchedVersion(Affected affected) {
        Optional<String> semVer = getFixed(affected, Range.Type.SEMVER);
        if (semVer.isPresent()) {
            return semVer;
        }
        Optional<String> ecoSystem = getFixed(affected, Range.Type.ECOSYSTEM);
        if (ecoSystem.isPresent()) {
            return ecoSystem;
        }
        return getFixed(affected, Range.Type.GIT);
    }

    private static Optional<String> getFixed(Affected affected,
            Range.Type rangeType) {
        Optional<Range> range = affected.getRanges().stream()
                .filter(r -> r.getType().equals(rangeType)).findFirst();
        if (range.isPresent()) {
            Optional<Object> fixed = range.get().getEvents().stream()
                    .map(event -> event.getAdditionalProperties().get("fixed"))
                    .filter(Objects::nonNull).findFirst();
            if (fixed.isPresent()) {
                return Optional.of((String) fixed.get());
            }
        }
        return Optional.empty();
    }

    private static String getVulnerabilityId(
            OpenSourceVulnerability vulnerability) {
        String identifier = vulnerability.getId();
        List<String> aliases = vulnerability.getAliases();
        if (aliases == null || identifier.startsWith("CVE")) {
            return identifier;
        } else {
            return vulnerability.getAliases().stream()
                    .filter(alias -> alias.startsWith("CVE")).findFirst()
                    .orElse(vulnerability.getId());
        }
    }

    private static Optional<AffectedVersion> getVaadinAnalysis(
            Vulnerability vulnerabilityDTO) {
        String vulnerabilityId = vulnerabilityDTO.getIdentifier();
        VulnerabilityDetails vulnerability = AppSecService.getInstance()
                .getVulnerabilityAnalysis().getVulnerabilities()
                .get(vulnerabilityId);
        if (vulnerability == null) {
            return Optional.empty();
        }
        Dependency dependency = vulnerabilityDTO.getDependency();
        String parentBomRef = dependency.getParentBomRef();
        String groupAndName = bomRefToGroupAndName(parentBomRef);
        Assessment assessment = vulnerability.getAssessments()
                .get(groupAndName);
        if (assessment == null) {
            return Optional.empty();
        }
        return assessment.getAffectedVersions().values().stream()
                .filter(v -> v.isInRange(bomRefToVersion(parentBomRef)))
                .findFirst();
    }

    private static String bomRefToGroupAndName(String bomRef) {
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        String[] bomRefParts = bomRef.split("/");
        String[] depParts = bomRefParts[2].split("@");
        return bomRefParts[1] + ":" + depParts[0];
    }

    private static String bomRefToVersion(String bomRef) {
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        String[] bomRefParts = bomRef.split("/");
        String[] depParts = bomRefParts[2].split("@");
        String[] verAndType = depParts[1].split("\\?");
        return verAndType[0];
    }
}
