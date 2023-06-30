/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;

public class GitHubServiceTest {

    private GitHubService service = new GitHubService();

    @Test
    public void getFramework7Versions() {
        List<String> versions = service.getFramework7Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(
                version -> MatcherAssert.assertThat(version, startsWith("7.")));
    }

    @Test
    public void getFramework8Versions() {
        List<String> versions = service.getFramework8Versions();

        assertEquals(GitHubService.NUMBER_OF_LATEST_MAINTAINED_VERSIONS,
                versions.size());
        versions.forEach(
                version -> MatcherAssert.assertThat(version, startsWith("8.")));
    }
}
