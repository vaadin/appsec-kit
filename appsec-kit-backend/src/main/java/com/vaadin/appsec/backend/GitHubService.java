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
import java.net.URL;
import java.text.Collator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            if (!(obj instanceof GitHubRelease)) {
                return false;
            }
            GitHubRelease other = (GitHubRelease) obj;
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

    static final String FLOW_RELEASES_URI = "https://api.github.com/repos/vaadin/platform/releases";

    static final Pattern FLOW_14_PATTERN = compile("^14\\.\\d+.\\d+$");

    static final Pattern FLOW_23_PATTERN = compile("^23\\.\\d+.\\d+$");

    static final long NUMBER_OF_LATEST_MAINTAINED_VERSIONS = 4;

    private List<GitHubRelease> releasesCache;

    private VulnerabilityAnalysis analysisCache;

    List<String> getFlow14Versions() {
        return getVersions(FLOW_14_PATTERN);
    }

    List<String> getFlow23Versions() {
        return getVersions(FLOW_23_PATTERN);
    }

    private List<String> getVersions(Pattern flowVersionPattern) {
        return getReleasesFromGitHub().stream().map(GitHubRelease::getTagName)
                .filter(flowVersionPattern.asPredicate())
                .limit(NUMBER_OF_LATEST_MAINTAINED_VERSIONS)
                .collect(Collectors.toList());
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
            URL flowReleasesUrl = getFlowReleasesUrl();
            releasesCache = listReader.readValue(flowReleasesUrl);
            LOGGER.debug("Vaadin releases cache updated from GitHub "
                    + releasesCache);
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
            analysisCache = jsonReader.readValue(analysisUrl);
            LOGGER.debug("Vaadin analysis cache updated from GitHub "
                    + analysisCache);
        } catch (IOException e) {
            throw new AppSecException("Cannot get Vaadin analysis from GitHub",
                    e);
        }
    }

    protected URL getFlowReleasesUrl() {
        try {
            return new URL(FLOW_RELEASES_URI);
        } catch (MalformedURLException e) {
            throw new AppSecException("Invalid Vaadin Flow releases URL", e);
        }
    }

    protected URL getVaadinAnalysisUrl() {
        try {
            return new URL(VAADIN_ANALYSIS_URI);
        } catch (MalformedURLException e) {
            throw new AppSecException("Invalid Vaadin analysis URL", e);
        }
    }
}
