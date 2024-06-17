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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
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
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;
import com.vaadin.appsec.backend.model.osv.response.Event;
import com.vaadin.appsec.backend.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.backend.model.osv.response.Range;
import com.vaadin.appsec.backend.model.osv.response.Severity;

/**
 * Helper class to provide bill of materials and vulnerabilities as DTOs for use
 * in the UI.
 */
class AppSecDTOProvider {

    private static final String INTRODUCED = "introduced";
    private static final String FIXED = "fixed";
    private static final String LAST_AFFECTED = "last_affected";
    private static final String LIMIT = "limit";

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

        final List<org.cyclonedx.model.Dependency> dependencies = new ArrayList<>(
                bomStore.getBom().getDependencies());

        final List<Component> components = new ArrayList<>(
                bomStore.getBom().getComponents());

        return components.stream().map(component -> {
            Ecosystem ecosystem = AppSecUtils.getEcosystem(component);
            Dependency dependency = new Dependency(ecosystem,
                    component.getGroup(), component.getName(),
                    component.getVersion());

            // Finds and sets the parent bom reference for this component
            dependencies.stream()
                    .filter(dep -> Objects.nonNull(dep.getDependencies())
                            && dep.getDependencies().stream()
                                    .anyMatch(transDep -> transDep.getRef()
                                            .equals(component.getBomRef())))
                    .findFirst().ifPresent(parent -> dependency
                            .setParentBomRef(parent.getRef()));

            updateVulnerabilityStatistics(dependency, vulnerabilities);

            return dependency;
        }).collect(Collectors.toList());
    }

    List<Vulnerability> getVulnerabilities() {
        final List<Dependency> dependencies = getDependencies();
        final List<OpenSourceVulnerability> vulnerabilities = vulnerabilityStore
                .getVulnerabilities();
        final List<Vulnerability> vulnerabilityDTOs = new ArrayList<>();

        for (OpenSourceVulnerability vuln : vulnerabilities) {
            for (Affected affected : vuln.getAffected()) {
                if (isMavenEcosystem(affected)) {
                    String vulnDepGroup = AppSecUtils.getVulnDepGroup(affected);
                    String vulnDepName = AppSecUtils.getVulnDepName(affected);
                    List<String> versions = affected.getVersions();
                    List<Range> ranges = affected.getRanges();

                    for (Dependency dep : dependencies) {
                        if (isVulnerable(dep, vulnDepGroup, vulnDepName,
                                versions, ranges)) {
                            Vulnerability vulnerabilityDTO = createVulnerabilityDTO(
                                    vuln, dep, affected);
                            vulnerabilityDTOs.add(vulnerabilityDTO);
                        }
                    }
                }
            }
        }

        return vulnerabilityDTOs;
    }

    private boolean isMavenEcosystem(Affected affected) {
        return Ecosystem.MAVEN.value()
                .equalsIgnoreCase(affected.getPackage().getEcosystem());
    }

    // Pseudocode for evaluating if a given version is affected
    // is available here https://ossf.github.io/osv-schema/#evaluation
    private boolean isVulnerable(Dependency dep, String vulnDepGroup,
            String vulnDepName, List<String> versions, List<Range> ranges) {
        return isSameGroup(dep.getGroup(), vulnDepGroup)
                && isSameName(dep.getName(), vulnDepName)
                && isVersionAffected(dep.getVersion(), versions, ranges);
    }

    private boolean isSameGroup(String depGroup, String vulnDepGroup) {
        return (depGroup == null && vulnDepGroup == null)
                || (depGroup != null && depGroup.equals(vulnDepGroup));
    }

    private boolean isSameName(String depName, String vulnDepName) {
        return depName.equals(vulnDepName);
    }

    private boolean isVersionAffected(String depVersion, List<String> versions,
            List<Range> ranges) {
        return includedInVersions(depVersion, versions)
                || includedInRanges(depVersion, ranges);
    }

    private boolean includedInVersions(String depVersion,
            List<String> versions) {
        return versions != null && versions.contains(depVersion);
    }

    private boolean includedInRanges(String depVersion, List<Range> ranges) {
        if (ranges != null) {
            DefaultArtifactVersion depArtifactVersion = new DefaultArtifactVersion(
                    depVersion);
            for (Range range : ranges) {
                List<Event> events = range.getEvents();
                if (beforeLimits(events, depArtifactVersion)
                        && evaluateEvents(events, depArtifactVersion)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean evaluateEvents(List<Event> events,
            ArtifactVersion version) {
        boolean vulnerable = false;
        for (Event event : events) {
            Optional<ArtifactVersion> introduced = getVersionFromEvent(event,
                    INTRODUCED);
            Optional<ArtifactVersion> fixed = getVersionFromEvent(event, FIXED);
            Optional<ArtifactVersion> lastAffected = getVersionFromEvent(event,
                    LAST_AFFECTED);

            if (introduced.isPresent()
                    && version.compareTo(introduced.get()) >= 0) {
                vulnerable = true;
            } else if (fixed.isPresent()
                    && version.compareTo(fixed.get()) >= 0) {
                vulnerable = false;
            } else if (lastAffected.isPresent()
                    && version.compareTo(lastAffected.get()) > 0) {
                vulnerable = false;
            }
        }
        return vulnerable;
    }

    private Optional<ArtifactVersion> getVersionFromEvent(Event event,
            String eventName) {
        if (event.getAdditionalProperties().containsKey(eventName)) {
            String version = (String) event.getAdditionalProperties()
                    .get(eventName);
            return Optional.of(new DefaultArtifactVersion(version));
        }
        return Optional.empty();
    }

    private boolean beforeLimits(List<Event> events, ArtifactVersion version) {
        boolean noLimitEvent = true;
        for (Event event : events) {
            if (event.getAdditionalProperties().containsKey(LIMIT)) {
                noLimitEvent = false;
                break;
            }
        }
        if (noLimitEvent) {
            return true;
        }

        for (Event event : events) {
            String limit = (String) event.getAdditionalProperties().get(LIMIT);
            ArtifactVersion artifactVersion = new DefaultArtifactVersion(limit);
            if (version.compareTo(artifactVersion) < 0) {
                return true;
            }
        }
        return false;
    }

    private Vulnerability createVulnerabilityDTO(OpenSourceVulnerability vuln,
            Dependency depDTO, Affected affected) {
        String id = getVulnerabilityId(vuln);
        Vulnerability vulnerabilityDTO = new Vulnerability(id);
        vulnerabilityDTO.setDependency(depDTO);
        vulnerabilityDTO.setDatePublished(vuln.getPublished());

        String patchedVersion = getPatchedVersion(affected).orElse("---");
        vulnerabilityDTO.setPatchedVersion(patchedVersion);

        if (vuln.getDetails() != null) {
            Node document = Parser.builder().build().parse(vuln.getDetails());
            vulnerabilityDTO.setDetails(
                    HtmlRenderer.builder().build().render(document));
        }

        Optional<AffectedVersion> vaadinAnalysis = getVaadinAnalysis(
                vulnerabilityDTO);
        vaadinAnalysis.ifPresent(affectedVersion -> vulnerabilityDTO
                .setVaadinAnalysis(affectedVersion.getStatus()));

        Map<String, AppSecData.VulnerabilityAssessment> vulnerabilityAssessments = AppSecService
                .getInstance().getData().getVulnerabilities();
        AppSecData.VulnerabilityAssessment vulnerabilityAssessment = vulnerabilityAssessments
                .get(id);
        if (vulnerabilityAssessment != null) {
            vulnerabilityDTO
                    .setDeveloperStatus(vulnerabilityAssessment.getStatus());
            vulnerabilityDTO.setDeveloperAnalysis(
                    vulnerabilityAssessment.getDeveloperAnalysis());
            vulnerabilityDTO
                    .setDeveloperUpdated(vulnerabilityAssessment.getUpdated());
        }

        Set<String> urls = new HashSet<>();
        vuln.getReferences().forEach(ref -> urls.add(ref.getUrl().toString()));
        vulnerabilityDTO.setReferenceUrls(urls);

        return vulnerabilityDTO;
    }

    private void updateVulnerabilityStatistics(Dependency dependency,
            List<OpenSourceVulnerability> vulnerabilities) {
        int vulnerabilityCount = 0;
        SeverityLevel highestSeverityLevel = SeverityLevel.NONE;
        Double highestScoreNumber = 0.0;
        String highestScoreString = "";

        for (OpenSourceVulnerability vulnerability : vulnerabilities) {
            for (Affected affected : vulnerability.getAffected()) {
                if (isMavenEcosystem(affected)) {
                    String depGroupAndName = AppSecUtils
                            .getDepGroupAndName(dependency);
                    if (depGroupAndName
                            .equals(affected.getPackage().getName())) {
                        vulnerabilityCount++;
                        highestSeverityLevel = findSeverityIfHigher(
                                vulnerability, highestSeverityLevel);
                        highestScoreNumber = findScoreIfHigher(vulnerability,
                                highestScoreNumber);
                        highestScoreString = getHighestCvssScoreString(
                                vulnerability, highestScoreNumber,
                                highestScoreString);
                    }
                }
            }
        }

        dependency.setNumOfVulnerabilities(vulnerabilityCount);
        dependency.setSeverityLevel(highestSeverityLevel);
        dependency.setRiskScore(highestScoreNumber);
        dependency.setCvssString(highestScoreString);
    }

    private SeverityLevel findSeverityIfHigher(
            OpenSourceVulnerability vulnerability,
            SeverityLevel highestSeverityLevel) {
        if (vulnerability.getSeverity() == null) {
            return highestSeverityLevel;
        }

        Double vulnScore = getHighestCvssScoreNumber(vulnerability);
        SeverityLevel severityLevel = SeverityLevel
                .getSeverityLevelForCvssScore(vulnScore);

        return SeverityLevelComparator.compareStatic(severityLevel,
                highestSeverityLevel) > 0 ? severityLevel
                        : highestSeverityLevel;
    }

    private Double findScoreIfHigher(OpenSourceVulnerability vulnerability,
            Double highestScore) {
        if (vulnerability.getSeverity() == null) {
            return highestScore;
        }
        Double vulnScore = getHighestCvssScoreNumber(vulnerability);
        return vulnScore > highestScore ? vulnScore : highestScore;
    }

    private Double getHighestCvssScoreNumber(
            OpenSourceVulnerability vulnerability) {
        return vulnerability.getSeverity().stream()
                .map(severity -> Cvss.fromVector(severity.getScore()))
                .map(cvss -> cvss.calculateScore().getBaseScore())
                .max(Comparator.naturalOrder()).orElse(0.0);
    }

    private String getHighestCvssScoreString(
            OpenSourceVulnerability vulnerability, Double highestScoreNumber,
            String highestScoreString) {
        if (vulnerability.getSeverity() == null) {
            return highestScoreString;
        }

        String cvssString = "";
        double tempBaseScore = 0.0;
        for (Severity severity : vulnerability.getSeverity()) {
            double baseScore = Cvss.fromVector(severity.getScore())
                    .calculateScore().getBaseScore();
            if (baseScore > tempBaseScore) {
                tempBaseScore = baseScore;
                cvssString = severity.getScore();
            }
        }

        return tempBaseScore >= highestScoreNumber ? cvssString
                : highestScoreString;
    }

    private Optional<String> getPatchedVersion(Affected affected) {
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

    private Optional<String> getFixed(Affected affected, Range.Type rangeType) {
        Optional<Range> range = affected.getRanges().stream()
                .filter(r -> r.getType().equals(rangeType)).findFirst();
        if (range.isPresent()) {
            Optional<Object> fixed = range.get().getEvents().stream()
                    .map(event -> event.getAdditionalProperties().get(FIXED))
                    .filter(Objects::nonNull).findFirst();
            if (fixed.isPresent()) {
                return Optional.of((String) fixed.get());
            }
        }
        return Optional.empty();
    }

    private String getVulnerabilityId(OpenSourceVulnerability vulnerability) {
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

    private Optional<AffectedVersion> getVaadinAnalysis(
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
        String groupAndName = AppSecUtils
                .bomRefToMavenGroupAndName(parentBomRef);
        Assessment assessment = vulnerability.getAssessments()
                .get(groupAndName);
        if (assessment == null) {
            return Optional.empty();
        }
        return assessment.getAffectedVersions().values().stream().filter(
                v -> v.isInRange(AppSecUtils.bomRefToVersion(parentBomRef)))
                .findFirst();
    }
}
