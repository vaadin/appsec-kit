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
import com.vaadin.appsec.backend.model.analysis.VulnerabilityAnalysis;
import com.vaadin.appsec.backend.model.analysis.VulnerabilityDetails;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;

public class GitHubServiceTest {

    static final String TEST_RESOURCE_BOM_PATH = "/bom.json";
    static final String TEST_RESOURCE_NPM_BOM_PATH = "/bom-npm.json";

    static class TestGitHubService1 extends GitHubService {

        @Override
        protected URL getFrameworkReleasesUrl() {
            return getClass().getClassLoader().getResource("releases.json");
        }

        @Override
        protected URL getFlowReleasesUrl() {
            return getClass().getClassLoader().getResource("releases.json");
        }
    }

    static class TestGitHubService2 extends TestGitHubService1 {

        @Override
        protected URL getVaadinAnalysisUrl() {
            return getClass().getClassLoader().getResource("analysis.json");
        }
    }

    private GitHubService service;

    @Before
    public void setup() throws Exception {
        AppSecConfiguration configuration = new AppSecConfiguration();
        configuration.setBomFilePath(Paths.get(AppSecServiceTest.class
                .getResource(TEST_RESOURCE_BOM_PATH).toURI()));
        configuration.setBomNpmFilePath(Paths.get(GitHubServiceTest.class
                .getResource(TEST_RESOURCE_NPM_BOM_PATH).toURI()));
        AppSecService.getInstance().setConfiguration(configuration);
        AppSecService.getInstance().init();
    }

    @Test
    public void getFramework7Versions() {
        service = new TestGitHubService2();
        List<String> versions = service.getFramework7Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(
                version -> MatcherAssert.assertThat(version, startsWith("7.")));
    }

    @Test
    public void getFramework8Versions() {
        service = new TestGitHubService2();
        List<String> versions = service.getFramework8Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(
                version -> MatcherAssert.assertThat(version, startsWith("8.")));
    }

    @Test
    public void getFlow24Versions() {
        service = new TestGitHubService2();
        List<String> versions = service.getFlow24Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(version -> MatcherAssert.assertThat(version,
                startsWith("24.")));
    }

    @Test
    public void getVulnerabilityAnalysisByURI() {
        service = new TestGitHubService2();
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

    @Test
    public void getVulnerabilityAnalysisByFile() {
        System.setProperty("vaadin.appsec.analysis",
                "src/test/resources/analysis.json");
        service = new TestGitHubService1();
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
