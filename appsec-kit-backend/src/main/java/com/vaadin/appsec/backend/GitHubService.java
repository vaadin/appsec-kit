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

import static java.util.regex.Pattern.compile;

class GitHubService {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubRelease implements Comparable<GitHubRelease> {

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
    }

    static final ObjectMapper MAPPER = new ObjectMapper();

    static final String FRAMEWORK_RELEASES_URI = "https://api.github.com/repos/vaadin/framework/releases";

    static final Pattern FRAMEWORK_8_PATTERN = compile("^8\\.\\d+.\\d+$");

    static final Pattern FRAMEWORK_7_PATTERN = compile("^7\\.\\d+.\\d+$");

    static final long NUMBER_OF_LATEST_MAINTAINED_VERSIONS = 4;

    private List<GitHubRelease> releasesCache;

    List<String> getFramework8Versions() {
        return getFrameworkVersions(FRAMEWORK_8_PATTERN);
    }

    List<String> getFramework7Versions() {
        return getFrameworkVersions(FRAMEWORK_7_PATTERN);
    }

    private List<String> getFrameworkVersions(Pattern frameworkVersionPattern) {
        return getReleasesFromGitHub().stream().map(GitHubRelease::getTagName)
                .filter(frameworkVersionPattern.asPredicate())
                .limit(NUMBER_OF_LATEST_MAINTAINED_VERSIONS)
                .collect(Collectors.toList());
    }

    private List<GitHubRelease> getReleasesFromGitHub() {
        if (releasesCache == null) {
            ObjectReader listReader = MAPPER
                    .readerForListOf(GitHubRelease.class);
            try {
                URL frameworkTagsUrl = new URL(FRAMEWORK_RELEASES_URI);
                releasesCache = listReader.readValue(frameworkTagsUrl);
            } catch (IOException e) {
                throw new AppSecException(
                        "Cannot get Vaadin releases from GitHub", e);
            }
        }
        return releasesCache;
    }
}
