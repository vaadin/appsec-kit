package com.vaadin.appsec.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.appsec.data.DependencyDTO;
import com.vaadin.appsec.data.SeverityLevel;
import com.vaadin.appsec.data.SeverityLevelComparator;
import com.vaadin.appsec.model.osv.response.Affected;
import com.vaadin.appsec.model.osv.response.OpenSourceVulnerability;

public class AppSecDataProvider {

    public static List<DependencyDTO> getDependencies() {
        final List<OpenSourceVulnerability> vulnerabilities = VulnerabilityStore.getInstance().getVulnerabilities();

        List<DependencyDTO> dependencies = BillOfMaterialsStore.getInstance().getBom().getComponents().stream()
                .map(c -> {
                    DependencyDTO dep = new DependencyDTO(c.getGroup(), c.getName(), c.getVersion());
                    String concatDepName = c.getGroup() + ":" + c.getName();
                    updateVulnerabilityStatistics(dep, vulnerabilities, concatDepName);
                    return dep;
                })
                .collect(Collectors.toList());
        return dependencies;
    }

    private static void updateVulnerabilityStatistics(DependencyDTO dependency, List<OpenSourceVulnerability> vulnerabilities, String concatDepName) {
        int vulnerabilityCount = 0;
        SeverityLevel highestSeverityLevel = SeverityLevel.NA;

        for (OpenSourceVulnerability vulnerability : vulnerabilities) {
            for (Affected affected : vulnerability.getAffected()) {
                if (concatDepName.equals(affected.getPackage().getName())) {
                    vulnerabilityCount++;
                    highestSeverityLevel = findSeverityIfHigher(vulnerability, highestSeverityLevel);
                    // TODO Calculate Risk Score & determine the highest score for this dependency
                }
            }
        }

        dependency.setSeverityLevel(highestSeverityLevel);
        dependency.setNumOfVulnerabilities(vulnerabilityCount);
    }

    private static SeverityLevel findSeverityIfHigher(OpenSourceVulnerability vulnerability, SeverityLevel highestSeverityLevel) {
        String severity = String.valueOf(vulnerability.databaseSpecific().getAdditionalProperties().get("severity"));
        SeverityLevel severityLevel = Arrays.stream(SeverityLevel.values())
                .filter(sl -> sl.name().equalsIgnoreCase((String) severity))
                .findAny()
                .orElse(SeverityLevel.NA);
        return SeverityLevelComparator.compareStatic(severityLevel, highestSeverityLevel) > 0 ? severityLevel : highestSeverityLevel;
    }
}
