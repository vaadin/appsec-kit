/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import org.cyclonedx.model.Component;

import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.osv.response.Affected;
import com.vaadin.appsec.backend.model.osv.response.Ecosystem;

/**
 * Helper class to provide information about dependencies and vulnerabilities.
 */
public class AppSecUtils {

    private AppSecUtils() {
    }

    /**
     * Gets ecosystem from a BOM component.
     *
     * @param component
     *            the component
     * @return the ecosystem the component belongs to
     */
    public static Ecosystem getEcosystem(Component component) {
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        String[] purlParts = component.getPurl().split("/");
        String[] pkgParts = purlParts[0].split(":");
        return Ecosystem.fromValue(pkgParts[1]);
    }

    /**
     * Gets the concatenated group and name of a BOM dependency.
     *
     * @param dependency
     *            the dependency
     * @return the concatenated group and name
     */
    public static String getDepGroupAndName(Dependency dependency) {
        // org.yaml:snakeyaml (group and name)
        return dependency.getGroup() + ":" + dependency.getName();
    }

    /**
     * Gets the concatenated group and name from a Maven BOM reference string.
     *
     * @param bomRef
     *            the BOM reference string
     * @return the concatenated group and name
     */
    public static String bomRefToMavenGroupAndName(String bomRef) {
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        String[] bomRefParts = bomRef.split("/");
        String[] dependencyParts = bomRefParts[2].split("@");
        return bomRefParts[1] + ":" + dependencyParts[0];
    }

    /**
     * Gets the version from a BOM reference string.
     *
     * @param bomRef
     *            the BOM reference string
     * @return the version
     */
    public static String bomRefToVersion(String bomRef) {
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        String[] bomRefParts = bomRef.split("/");
        String[] dependencyParts = bomRefParts[bomRefParts.length - 1]
                .split("@");
        String[] versionAndType = dependencyParts[1].split("\\?");
        return versionAndType[0];
    }

    /**
     * Gets the group of a dependency the vulnerability belongs to.
     *
     * @param affected
     *            the affected dependency
     * @return the dependency group
     */
    public static String getVulnDepGroup(Affected affected) {
        // org.yaml:snakeyaml (group and name)
        String depName = affected.getPackage().getName();
        return depName.split(":")[0];
    }

    /**
     * Gets the name of a dependency the vulnerability belongs to.
     *
     * @param affected
     *            the affected dependency
     * @return the dependency name
     */
    public static String getVulnDepName(Affected affected) {
        // org.yaml:snakeyaml (group and name)
        String depName = affected.getPackage().getName();
        return depName.split(":")[1];
    }
}
