/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.model.analysis.VulnerabilityAnalysis;

import static java.util.regex.Pattern.compile;

class GitHubService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GitHubService.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GitHubRelease implements Comparable<GitHubRelease> {

        @JsonProperty("tag_name")
        private String tagName;

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String name) {
            this.tagName = name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tagName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GitHubRelease other)) {
                return false;
            }
            return Objects.equals(tagName, other.tagName);
        }

        @Override
        public int compareTo(GitHubRelease o) {
            return Collator.getInstance().compare(tagName, o.tagName);
        }

        @Override
        public String toString() {
            return "GitHubRelease{" + "tagName='" + tagName + '\'' + '}';
        }
    }

    static final ObjectMapper MAPPER = new ObjectMapper();

    static final String VAADIN_ANALYSIS_URI = "https://raw.githubusercontent.com/vaadin/vulnerability-analysis/main/analysis.json";

    static final String FLOW_RELEASES_URI = "https://api.github.com/repos/vaadin/flow/releases";

    static final Pattern FLOW_VERSION_PATTERN = compile("^25\\.\\d+.\\d+$");

    static final long NUMBER_OF_LATEST_MAINTAINED_VERSIONS = 4;

    static final String ANALYSIS_PATH_AND_NAME_PROPERTY = "vaadin.appsec.analysis";

    private List<GitHubRelease> releasesCache;

    private VulnerabilityAnalysis analysisCache;

    List<String> getFlowVersions() {
        return getReleasesFromGitHub().stream().map(GitHubRelease::getTagName)
                .filter(GitHubService.FLOW_VERSION_PATTERN.asPredicate())
                .limit(NUMBER_OF_LATEST_MAINTAINED_VERSIONS).toList();
    }

    private List<GitHubRelease> getReleasesFromGitHub() {
        if (releasesCache == null) {
            updateReleasesCache();
        }
        return releasesCache;
    }

    void updateReleasesCache() {
        ObjectReader listReader = MAPPER.readerForListOf(GitHubRelease.class);
        try {
            URL releasesUrl = getFlowReleasesUrl();
            releasesCache = listReader.readValue(releasesUrl.openStream());
            LOGGER.debug("Vaadin releases cache updated from GitHub {}",
                    releasesCache);
        } catch (IOException e) {
            throw new AppSecException("Cannot get Vaadin releases from GitHub",
                    e);
        }
    }

    VulnerabilityAnalysis getVulnerabilityAnalysis() {
        if (analysisCache == null) {
            updateAnalysisCache();
        }
        return analysisCache;
    }

    void updateAnalysisCache() {
        ObjectReader jsonReader = MAPPER.readerFor(VulnerabilityAnalysis.class);
        try {
            URL analysisUrl = getVaadinAnalysisUrl();
            analysisCache = jsonReader.readValue(analysisUrl.openStream());
            LOGGER.debug("Vaadin analysis cache updated from GitHub {}",
                    analysisCache);
        } catch (IOException e) {
            throw new AppSecException("Cannot get Vaadin analysis from GitHub",
                    e);
        }
    }

    protected URL getFlowReleasesUrl() {
        try {
            return URI.create(FLOW_RELEASES_URI).toURL();
        } catch (MalformedURLException e) {
            throw new AppSecException("Invalid Vaadin Flow releases URL", e);
        }
    }

    protected URL getVaadinAnalysisUrl() {
        String analysisPathAndName = System
                .getProperty(ANALYSIS_PATH_AND_NAME_PROPERTY);
        if (analysisPathAndName != null && !analysisPathAndName.isEmpty()) {
            Path analysis = null;
            try {
                analysis = Paths.get(analysisPathAndName);
                return analysis.toUri().toURL();
            } catch (InvalidPathException e) {
                throw new AppSecException(
                        "Invalid Vaadin analysis file path and name "
                                + analysis,
                        e);
            } catch (MalformedURLException e) {
                throw new AppSecException(
                        "Invalid Vaadin analysis file path and name URL", e);
            }
        } else {
            try {
                return URI.create(VAADIN_ANALYSIS_URI).toURL();
            } catch (MalformedURLException e) {
                throw new AppSecException("Invalid Vaadin analysis URL", e);
            }
        }
    }
}
