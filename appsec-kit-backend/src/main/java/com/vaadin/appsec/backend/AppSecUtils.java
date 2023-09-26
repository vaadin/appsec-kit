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
        // purl (package URL) types:
        // pkg:maven/com.vaadin/vaadin-server@8.13.0?type=jar
        // pkg:npm/@cyclonedx/cyclonedx-npm@1.14.0
        // pkg:npm/npmlog@5.0.1
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
     * Gets the concatenated group and name from a npm BOM reference string.
     *
     * @param bomRef
     *            the BOM reference string
     * @return the concatenated group and name
     */
    public static String bomRefToNpmGroupAndName(String bomRef) {
        String groupAndName;
        if (bomRef.contains("/")) {
            // @cyclonedx/cyclonedx-npm@1.14.0
            String[] bomRefParts = bomRef.split("/");
            String[] dependencyParts = bomRefParts[1].split("@");
            groupAndName = bomRefParts[1] + ":" + dependencyParts[0];
        } else {
            // npmlog@5.0.1
            String[] bomRefParts = bomRef.split("@");
            groupAndName = bomRefParts[0];
        }
        return groupAndName;
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
        // @cyclonedx/cyclonedx-npm@1.14.0
        // npmlog@5.0.1
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

    /**
     * Gets the name of a dependency the vulnerability belongs to.
     *
     * @param affected
     *            the affected dependency
     * @return the dependency name
     */
    public static String getVulnDepName(Affected affected) {
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
}
