/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.Property;
import org.cyclonedx.parsers.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.model.osv.response.Ecosystem;

/**
 * Provides means to store and fetch bill of materials from a static instance.
 */
class BillOfMaterialsStore {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BillOfMaterialsStore.class);

    static final String DEVELOPMENT_PROPERTY_NAME = "cdx:npm:package:development";
    private static final String NO_NAME_REF = "-/no-name@-";

    private Bom bomMaven;
    private Bom bomNpm;

    BillOfMaterialsStore() {
    }

    Bom getBom(Ecosystem ecosystem) {
        if (Objects.requireNonNull(ecosystem) == Ecosystem.MAVEN) {
            return bomMaven;
        } else {
            return bomNpm;
        }
    }

    void readBomFile(Path bomFilePath, Ecosystem ecosystem)
            throws ParseException {
        if (Objects.requireNonNull(ecosystem) == Ecosystem.MAVEN) {
            bomMaven = readBomFile(bomFilePath);
        } else {
            boolean includeNpmDevDeps = AppSecService.getInstance()
                    .getConfiguration().isIncludeNpmDevDependencies();
            bomNpm = includeNpmDevDeps ? readBomFile(bomFilePath)
                    : filterOutNpmDevDeps(readBomFile(bomFilePath));
        }
        LOGGER.debug("Reading SBOM from file " + bomFilePath.toAbsolutePath());
    }

    private Bom readBomFile(Path bomFilePath) throws ParseException {
        JsonParser parser = new JsonParser();
        File bomFile = bomFilePath.toFile();
        try {
            return parser.parse(bomFile);
        } catch (ParseException e) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            try (InputStream is = ccl
                    .getResourceAsStream(bomFilePath.toString())) {
                if (is != null) {
                    return parser.parse(is);
                } else {
                    // Throw original ParseException if resource stream is null
                    throw e;
                }
            } catch (IOException ex) {
                throw new AppSecException(
                        "SBOM file not found on path " + bomFilePath, ex);
            }
        }
    }

    private Bom filterOutNpmDevDeps(Bom bom) {
        List<String> npmDevDepBomRefs = new ArrayList<>();
        filterOutComponents(bom, npmDevDepBomRefs);
        filterOutDependencies(bom, npmDevDepBomRefs);
        return bom;
    }

    private void filterOutComponents(Bom bom, List<String> npmDevDepBomRefs) {
        List<Component> componentsToInclude = new ArrayList<>();
        for (Component component : bom.getComponents()) {
            boolean isDevDep = false;
            for (Property property : component.getProperties()) {
                if (property.getName().equals(DEVELOPMENT_PROPERTY_NAME)
                        && property.getValue().equals("true")) {
                    isDevDep = true;
                    npmDevDepBomRefs.add(component.getBomRef());
                }
            }
            if (!isDevDep) {
                componentsToInclude.add(component);
            }
        }
        bom.setComponents(componentsToInclude);
    }

    private void filterOutDependencies(Bom bom, List<String> npmDevDepBomRefs) {
        List<Dependency> dependenciesToInclude = new ArrayList<>();
        for (Dependency dependency : bom.getDependencies()) {
            if (dependency.getRef().equals(NO_NAME_REF)) {
                List<Dependency> dependsOnDepsToInclude = new ArrayList<>();
                List<Dependency> dependsOnDeps = dependency.getDependencies();
                for (Dependency dependsOnDep : dependsOnDeps) {
                    if (!npmDevDepBomRefs.contains(dependsOnDep.getRef())) {
                        dependsOnDepsToInclude.add(dependsOnDep);
                    }
                }
                dependency.setDependencies(dependsOnDepsToInclude);
                dependenciesToInclude.add(dependency);
            } else if (!npmDevDepBomRefs.contains(dependency.getRef())) {
                dependenciesToInclude.add(dependency);
            }
        }
        bom.setDependencies(dependenciesToInclude);
    }
}