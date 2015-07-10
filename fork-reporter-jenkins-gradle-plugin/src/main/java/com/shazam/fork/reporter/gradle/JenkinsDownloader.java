/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter.gradle;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;

import org.gradle.api.GradleException;

import java.io.*;
import java.net.*;
import java.util.List;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.asByteSource;
import static com.shazam.fork.CommonDefaults.*;
import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.stripEnd;

class JenkinsDownloader {

    private final File forkSummariesDir;
    private final ForkJenkinsReportExtension extension;

    JenkinsDownloader(File forkSummariesDir, ForkJenkinsReportExtension extension) {
        this.forkSummariesDir = forkSummariesDir;
        this.extension = extension;
    }

    void downloadJenkinsFiles() {
        try {
            JenkinsServer jenkinsServer = getJenkinsServer();
            JobWithDetails job = jenkinsServer.getJob(extension.jenkinsJobName);

            job.getBuilds().stream()
                    .forEach(build -> getDetailsFromBuild(build).stream()
                            .filter(this::isForkSummaryArtifact)
                            .forEach(artifact -> downloadArtifact(build, artifact)));
        } catch (IOException e) {
            throw new GradleException("Could not get reports from Jenkins Server", e);
        }
    }

    private List<Artifact> getDetailsFromBuild(Build build) {
        try {
            return build.details().getArtifacts();
        } catch (IOException e) {
            throw new GradleException("Could not fetch details or artifacts of build: " + build.getUrl(), e);
        }
    }

    @Nonnull
    private JenkinsServer getJenkinsServer() {
        try {
            URI serverUri = new URI(extension.jenkinsUrl);
            String username = extension.jenkinsUsername;
            String password = extension.jenkinsPassword;
            if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
                return new JenkinsServer(serverUri);
            }
            return new JenkinsServer(serverUri, username, password);
        } catch (URISyntaxException e) {
            throw new GradleException("Error when creating URI for Jenkins server on: " + extension.jenkinsUrl, e);
        }
    }

    private void downloadArtifact(Build build, Artifact artifact) {
        FileOutputStream output = null;
        URL artifactUrl = getArtifactUrl(build, artifact);
        try {
            File summaryFile = new File(forkSummariesDir, format(FORK_SUMMARY_FILENAME_FORMAT, build.getNumber()));
            output = new FileOutputStream(summaryFile);
            asByteSource(artifactUrl).copyTo(output);
        } catch (IOException e) {
            throw new GradleException("Could not download artifact from: " + artifactUrl.toString(), e);
        } finally {
            closeQuietly(output);
        }
    }

    String createBaseUrl() {
        String jenkinsReportTitle = encodeReportTitle(extension.jenkinsReportTitle);
        if (isNullOrEmpty(jenkinsReportTitle)) {
            return null;
        }
        String pattern = "%s/job/%s/%s/%s";
        String url = stripEnd(extension.jenkinsUrl, "/");
        String encodedJobName = encode(extension.jenkinsJobName);

        return String.format(pattern, url, encodedJobName, BUILD_ID_TOKEN, jenkinsReportTitle);
    }

    /**
     * Jenkins doesn't properly URL encode the report titles. The only noticable change is it replaces spaces with
     * underscores.
     */
    private String encodeReportTitle(String jenkinsReportTitle) {
        return jenkinsReportTitle.replaceAll("\\s", "_");
    }

    @SuppressWarnings("deprecation")
    private String encode(String pathPart) {
        return URLEncoder.encode(pathPart).replaceAll("\\+", "%20");
    }


    @Nonnull
    private URL getArtifactUrl(Build build, Artifact artifact) {
        try {
            URI uri = new URI(build.getUrl());
            String artifactPath = uri.getPath() + "artifact/" + artifact.getRelativePath();
            URI artifactUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), artifactPath, "", "");
            return artifactUri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new GradleException("Error when trying to construct artifact URL for: " + build.getUrl(), e);
        }
    }

    private boolean isForkSummaryArtifact(Artifact artifact) {
        return artifact.getFileName().matches(FORK_SUMMARY_FILENAME_REGEX);
    }
}
