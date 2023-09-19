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

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Property;
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
import com.vaadin.appsec.backend.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.backend.model.osv.response.Range;
import com.vaadin.appsec.backend.model.osv.response.Severity;

import static com.vaadin.appsec.backend.BillOfMaterialsStore.DEVELOPMENT_PROPERTY_NAME;

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

        final List<org.cyclonedx.model.Dependency> dependencies = new ArrayList<>(
                bomStore.getBom(Ecosystem.MAVEN).getDependencies());
        if (bomStore.getBom(Ecosystem.NPM) != null) {
            dependencies
                    .addAll(bomStore.getBom(Ecosystem.NPM).getDependencies());
        }

        final List<Component> components = new ArrayList<>(
                bomStore.getBom(Ecosystem.MAVEN).getComponents());
        if (bomStore.getBom(Ecosystem.NPM) != null) {
            components.addAll(bomStore.getBom(Ecosystem.NPM).getComponents());
        }

        return components.stream().map(component -> {
            Ecosystem ecosystem = getEcosystem(component);
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

            // Verifies and sets if a npm dependency is a dev dependency
            // For Maven dependencies this value is always false
            if (dependency.getEcosystem() == Ecosystem.NPM) {
                dependency.setDevDependency(isDevDependency(component));
            }

            updateVulnerabilityStatistics(dependency, vulnerabilities);

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
        for (OpenSourceVulnerability vuln : vulnerabilities) {
            for (Affected affected : vuln.getAffected()) {
                String vulnDepGroup = getVulnDepGroup(affected);
                String vulnDepName = getVulnDepName(affected);

                Dependency depDTO = null;
                for (Dependency dep : dependencies) {
                    if (((dep.getGroup() == null && vulnDepGroup == null)
                            || (dep.getGroup() != null
                                    && dep.getGroup().equals(vulnDepGroup)))
                            && dep.getName().equals(vulnDepName) && affected
                                    .getVersions().contains(dep.getVersion())) {
                        depDTO = dep;
                    }
                }

                if (depDTO != null) {
                    String id = getVulnerabilityId(vuln);
                    Vulnerability vulnerabilityDTO = new Vulnerability(id);
                    vulnerabilityDTO.setDependency(depDTO);
                    vulnerabilityDTO.setDatePublished(vuln.getPublished());

                    String patchedVersion = getPatchedVersion(affected)
                            .orElse("---");
                    vulnerabilityDTO.setPatchedVersion(patchedVersion);

                    if (vuln.getDetails() != null) {
                        Node document = parser.parse(vuln.getDetails());
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
                    vuln.getReferences()
                            .forEach(ref -> urls.add(ref.getUrl().toString()));
                    vulnerabilityDTO.setReferenceUrls(urls);

                    vulnerabilityDTOS.add(vulnerabilityDTO);
                }
            }
        }
        return vulnerabilityDTOS;
    }

    private boolean isDevDependency(Component component) {
        for (Property property : component.getProperties()) {
            if (property.getName().equals(DEVELOPMENT_PROPERTY_NAME)
                    && property.getValue().equals("true")) {
                return true;
            }
        }
        return false;
    }

    private static void updateVulnerabilityStatistics(Dependency dependency,
            List<OpenSourceVulnerability> vulnerabilities) {
        int vulnerabilityCount = 0;
        SeverityLevel highestSeverityLevel = SeverityLevel.NONE;
        Double highestScoreNumber = 0.0;
        String highestScoreString = "";

        for (OpenSourceVulnerability vulnerability : vulnerabilities) {
            for (Affected affected : vulnerability.getAffected()) {
                String depGroupAndName = getDepGroupAndName(dependency);
                if (depGroupAndName.equals(affected.getPackage().getName())) {
                    vulnerabilityCount++;
                    highestSeverityLevel = findSeverityIfHigher(vulnerability,
                            highestSeverityLevel);
                    highestScoreNumber = findScoreIfHigher(vulnerability,
                            highestScoreNumber);
                    highestScoreString = getHighestCvssScoreString(
                            vulnerability, highestScoreNumber,
                            highestScoreString);
                }
            }
        }

        dependency.setNumOfVulnerabilities(vulnerabilityCount);
        dependency.setSeverityLevel(highestSeverityLevel);
        dependency.setRiskScore(highestScoreNumber);
        dependency.setCvssString(highestScoreString);
    }

    private static SeverityLevel findSeverityIfHigher(
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

    private static Double findScoreIfHigher(
            OpenSourceVulnerability vulnerability, Double highestScore) {
        if (vulnerability.getSeverity() == null) {
            return highestScore;
        }
        Double vulnScore = getHighestCvssScoreNumber(vulnerability);
        return vulnScore > highestScore ? vulnScore : highestScore;
    }

    private static Double getHighestCvssScoreNumber(
            OpenSourceVulnerability vulnerability) {
        return vulnerability.getSeverity().stream()
                .map(severity -> Cvss.fromVector(severity.getScore()))
                .map(cvss -> cvss.calculateScore().getBaseScore())
                .max(Comparator.naturalOrder()).orElse(0.0);
    }

    private static String getHighestCvssScoreString(
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

        return tempBaseScore > highestScoreNumber ? cvssString
                : highestScoreString;
    }

    private static Ecosystem getEcosystem(Component component) {
        // purl (package URL) types:
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        // pkg:npm/@cyclonedx/cyclonedx-npm@1.14.0
        // pkg:npm/npmlog@5.0.1
        String[] purlParts = component.getPurl().split("/");
        String[] pkgParts = purlParts[0].split(":");
        return Ecosystem.fromValue(pkgParts[1]);
    }

    private static String getDepGroupAndName(Dependency dependency) {
        // Dependency name types:
        // org.yaml:snakeyaml (Maven with group and name)
        // @strapi/admin (npm with group and name)
        // electron (npm with no group)
        if (dependency.getEcosystem() == Ecosystem.NPM) {
            if (dependency.getGroup() == null) {
                return dependency.getName();
            }
            return dependency.getGroup() + "/" + dependency.getName();
        }
        return dependency.getGroup() + ":" + dependency.getName();
    }

    private static String getVulnDepGroup(Affected affected) {
        // Dependency name types:
        // org.yaml:snakeyaml (Maven with group and name)
        // @strapi/admin (npm with group and name)
        // electron (npm with no group)
        String depName = affected.getPackage().getName();
        String ecosystem = affected.getPackage().getEcosystem();
        if (ecosystem.equals(Ecosystem.NPM.toString())) {
            String[] nameParts = depName.split("/");
            if (nameParts.length == 2) {
                return nameParts[0];
            }
            return null;
        }
        return depName.split(":")[0];
    }

    private static String getVulnDepName(Affected affected) {
        // Dependency name types:
        // org.yaml:snakeyaml (Maven with group and name)
        // @strapi/admin (npm with group and name)
        // electron (npm with no group)
        String depName = affected.getPackage().getName();
        String ecosystem = affected.getPackage().getEcosystem();
        if (ecosystem.equals(Ecosystem.NPM.toString())) {
            String[] nameParts = depName.split("/");
            if (nameParts.length == 2) {
                return nameParts[1];
            }
            return depName;
        }
        return depName.split(":")[1];
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
