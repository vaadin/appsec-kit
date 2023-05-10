package com.vaadin.appsec.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.cyclonedx.model.Component;
import us.springett.cvss.Cvss;

import com.vaadin.appsec.data.DependencyDTO;
import com.vaadin.appsec.data.SeverityLevel;
import com.vaadin.appsec.data.SeverityLevelComparator;
import com.vaadin.appsec.data.VulnerabilityDTO;
import com.vaadin.appsec.model.osv.response.Affected;
import com.vaadin.appsec.model.osv.response.OpenSourceVulnerability;

public class AppSecDataProvider {

    public static List<DependencyDTO> getDependencies() {
        final List<OpenSourceVulnerability> vulnerabilities = VulnerabilityStore.getInstance().getVulnerabilities();

        return BillOfMaterialsStore.getInstance().getBom().getComponents().stream()
                .map(c -> {
                    DependencyDTO dep = new DependencyDTO(c.getGroup(), c.getName(), c.getVersion());
                    updateVulnerabilityStatistics(dep, vulnerabilities, getConcatDepName(c));
                    return dep;
                })
                .collect(Collectors.toList());
    }

    public static List<VulnerabilityDTO> getVulnerabilities() {
        final List<DependencyDTO> dependencies = getDependencies();
        final List<OpenSourceVulnerability> vulnerabilities = VulnerabilityStore.getInstance().getVulnerabilities();

        List<VulnerabilityDTO> vulnerabilityDTOS = new ArrayList<>();
        for (OpenSourceVulnerability v : vulnerabilities) {
            for (Affected affected : v.getAffected()) {
                String depGroup = getDepGroup(affected);
                String depName = getDepName(affected);
                DependencyDTO dependencyDTO = dependencies.stream().filter(d -> d.getGroup().equals(depGroup) && d.getName().equals(depName)).findFirst().orElse(null);
                if (dependencyDTO != null) {
                    VulnerabilityDTO vulnerabilityDTO = new VulnerabilityDTO(v.getId());
                    vulnerabilityDTO.setDependency(dependencyDTO);
                    vulnerabilityDTOS.add(vulnerabilityDTO);
                }
            }
        }
        return vulnerabilityDTOS;
    }

    private static void updateVulnerabilityStatistics(DependencyDTO dependency, List<OpenSourceVulnerability> vulnerabilities, String concatDepName) {
        int vulnerabilityCount = 0;
        SeverityLevel highestSeverityLevel = SeverityLevel.NA;
        Double highestScore = 0.0;

        for (OpenSourceVulnerability vulnerability : vulnerabilities) {
            for (Affected affected : vulnerability.getAffected()) {
                if (concatDepName.equals(affected.getPackage().getName())) {
                    vulnerabilityCount++;
                    highestSeverityLevel = findSeverityIfHigher(vulnerability, highestSeverityLevel);
                    highestScore = findScoreIfHigher(vulnerability, highestScore);
                }
            }
        }

        dependency.setNumOfVulnerabilities(vulnerabilityCount);
        dependency.setSeverityLevel(highestSeverityLevel);
        dependency.setRiskScore(highestScore);
    }

    private static SeverityLevel findSeverityIfHigher(OpenSourceVulnerability vulnerability, SeverityLevel highestSeverityLevel) {
        String severity = String.valueOf(vulnerability.databaseSpecific().getAdditionalProperties().get("severity"));
        SeverityLevel severityLevel = Arrays.stream(SeverityLevel.values())
                .filter(sl -> sl.name().equalsIgnoreCase((String) severity))
                .findAny()
                .orElse(SeverityLevel.NA);
        return SeverityLevelComparator.compareStatic(severityLevel, highestSeverityLevel) > 0 ? severityLevel : highestSeverityLevel;
    }

    private static Double findScoreIfHigher(OpenSourceVulnerability vulnerability, Double highestScore) {
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
}
