/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.appsec.backend.model.analysis.Assessment;
import com.vaadin.appsec.backend.model.analysis.VulnerabilityDetails;
import com.vaadin.appsec.backend.model.analysis.VulnerabilityAnalysis;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;

public class GitHubServiceTest {

    static final String TEST_RESOURCE_MAVEN_BOM_PATH = "/bom.json";
    static final String TEST_RESOURCE_NPM_BOM_PATH = "/bom-npm.json";

    static class TestGitHubService extends GitHubService {

        @Override
        protected URL getFlowReleasesUrl() {
            return getClass().getClassLoader().getResource("releases.json");
        }

        @Override
        protected URL getVaadinAnalysisUrl() {
            return getClass().getClassLoader().getResource("analysis.json");
        }
    }

    private GitHubService service;

    @Before
    public void setup() throws Exception {
        service = new TestGitHubService();

        AppSecConfiguration configuration = new AppSecConfiguration();
        configuration.setBomFilePath(Paths.get(GitHubServiceTest.class
                .getResource(TEST_RESOURCE_MAVEN_BOM_PATH).toURI()));
        configuration.setBomNpmFilePath(Paths.get(GitHubServiceTest.class
                .getResource(TEST_RESOURCE_NPM_BOM_PATH).toURI()));
        AppSecService.getInstance().setConfiguration(configuration);
        AppSecService.getInstance().init();
    }

    @Test
    public void getFlow14Versions() {
        List<String> versions = service.getFlow14Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(
                version -> MatcherAssert.assertThat(version, startsWith("14.")));
    }

    @Test
    public void getFlow23Versions() {
        List<String> versions = service.getFlow23Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(version -> MatcherAssert.assertThat(version,
                startsWith("23.")));
    }

    @Test
    public void getVulnerabilityAnalysis() {
        VulnerabilityAnalysis vulnerabilityAnalysis = service
                .getVulnerabilityAnalysis();

        VulnerabilityDetails vulnerability = vulnerabilityAnalysis
                .getVulnerabilities().entrySet().stream().findFirst().get()
                .getValue();

        assertEquals("org.acme:foobar",
                vulnerability.getDependency().getName());

        Assessment assessment = vulnerability.getAssessments().entrySet()
                .stream().findFirst().get().getValue();

        int affectedVersionsSize = assessment.getAffectedVersions().size();

        assertEquals(2, affectedVersionsSize);
    }
}
