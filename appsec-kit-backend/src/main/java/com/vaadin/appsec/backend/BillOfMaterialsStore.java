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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static final String DEVELOPMENT_PROPERTY_NAME = "cdx:npm:package:development";
    private static final String NO_NAME_REF = "-/no-name@-";
    private static final String PLATFORM_COMBINED_BOM = "https://github.com/vaadin/platform/releases/download/%s/Software.Bill.Of.Materials.json";

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
                    : filterOutNpmDevLibraries(readBomFile(bomFilePath));
        }
        LOGGER.debug("Reading SBOM from file " + bomFilePath.toAbsolutePath());
    }

    void readPlatformCombinedBomFile() {
        ObjectReader jsonReader = MAPPER.readerFor(Bom.class);
        try {
            URL platformCombinedBomUrl = getPlatformCombinedBomUrl();
            Bom platformCombinedBom = jsonReader
                    .readValue(platformCombinedBomUrl);
            boolean includeNpmDevDeps = AppSecService.getInstance()
                    .getConfiguration().isIncludeNpmDevDependencies();
            bomNpm = includeNpmDevDeps
                    ? filterOutMavenLibraries(platformCombinedBom)
                    : filterOutNpmDevLibraries(
                            filterOutMavenLibraries(platformCombinedBom));
            LOGGER.debug("Reading SBOM from Vaadin platform");
        } catch (IOException e) {
            throw new AppSecException("Cannot get Vaadin platform SBOM", e);
        }
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

    private Bom filterOutNpmDevLibraries(Bom bom) {
        List<String> npmDevDepBomRefs = new ArrayList<>();
        filterOutNpmDevComponents(bom, npmDevDepBomRefs);
        filterOutNpmDevDependencies(bom, npmDevDepBomRefs);
        return bom;
    }

    private void filterOutNpmDevComponents(Bom bom,
            List<String> npmDevDepBomRefs) {
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

    private void filterOutNpmDevDependencies(Bom bom,
            List<String> npmDevDepBomRefs) {
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

    private Bom filterOutMavenLibraries(Bom bom) {
        List<String> mavenPurls = new ArrayList<>();
        filterOutMavenComponents(bom, mavenPurls);
        filterOutMavenDependencies(bom, mavenPurls);
        return bom;
    }

    private void filterOutMavenComponents(Bom bom, List<String> mavenPurls) {
        List<Component> componentsToInclude = new ArrayList<>();
        for (Component component : bom.getComponents()) {
            boolean isMavenComp = false;
            Ecosystem ecosystem = AppSecUtils.getEcosystem(component);
            if (ecosystem == Ecosystem.MAVEN) {
                isMavenComp = true;
                mavenPurls.add(component.getPurl());
            }
            if (!isMavenComp) {
                componentsToInclude.add(component);
            }
        }
        bom.setComponents(componentsToInclude);
    }

    private void filterOutMavenDependencies(Bom bom, List<String> mavenPurls) {
        List<Dependency> dependenciesToInclude = new ArrayList<>();
        for (Dependency dependency : bom.getDependencies()) {
            if (!mavenPurls.contains(dependency.getRef())) {
                dependenciesToInclude.add(dependency);
            }
        }
        bom.setDependencies(dependenciesToInclude);
    }

    private URL getPlatformCombinedBomUrl() {
        String version;
        Optional<Component> flowServerComponent = AppSecService.getInstance()
                .getFlowServerComponent();
        if (flowServerComponent.isPresent()) {
            version = flowServerComponent.get().getVersion();
        } else {
            throw new AppSecException("Cannot get Vaadin platform version.");
        }
        try {
            return new URL(String.format(PLATFORM_COMBINED_BOM, version));
        } catch (MalformedURLException e) {
            throw new AppSecException("Invalid Vaadin platform SBOM URL", e);
        }
    }
}
