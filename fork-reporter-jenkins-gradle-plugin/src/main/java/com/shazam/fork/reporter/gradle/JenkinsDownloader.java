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
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.JobWithDetails;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.asByteSource;
import static com.shazam.fork.CommonDefaults.BUILD_ID_TOKEN;
import static com.shazam.fork.CommonDefaults.FORK_SUMMARY_FILENAME_FORMAT;
import static com.shazam.fork.CommonDefaults.FORK_SUMMARY_FILENAME_REGEX;
import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.stripEnd;

class JenkinsDownloader {

    private final File forkSummariesDir;
    private final ForkJenkinsReportExtension extension;
    private final String baseJenkinsUrl;

    JenkinsDownloader(File forkSummariesDir, ForkJenkinsReportExtension extension) {
        this.forkSummariesDir = forkSummariesDir;
        this.extension = extension;
        this.baseJenkinsUrl = stripEnd(extension.jenkinsUrl, "/");
    }

    @Nullable
    String downloadJenkinsFiles() {
        try {
            JobDetails jobDetails = getJob();
            JobWithDetails jobWithDetails = jobDetails.jobWithDetails;
            if (jobWithDetails == null) {
                throw new GradleException("No jobs found for your current configuration");
            }

            jobWithDetails
                    .getBuilds()
                    .stream()
                    .forEach(build -> getDetailsFromBuild(build).stream()
                            .filter(this::isForkSummaryArtifact)
                            .forEach(artifact -> downloadArtifact(build, artifact)));
            return jobDetails.reportUrlTemplate;
        } catch (IOException e) {
            throw new GradleException("Could not get reports from Jenkins Server", e);
        }
    }

    /**
     * This method caters for differences between freestyle and folder jobs in Jenkins.
     * Freestyle job URLs would look like:
     * <ul>
     *     <li>Job: <code>https://jenkins.server/job/JobName</code></li>
     *     <li>Report: <code>https://jenkins.server/job/JobName/1111/Fork_Report/</code></li>
     * </ul>
     * The folder equivalent would be:
     * <ul>
     *     <li>Job:  <code>https://jenkins.server/job/Folder/job/JobName/</code></li>
     *     <li>Report:  <code>https://jenkins.server/job/Folder/job/JobName/1111/Fork_Report/</code></li>
     * </ul>
     */
    private JobDetails getJob() throws IOException {
        @Nullable String encodedReportTitle = encodedReportTitle();
        switch (getJobType(extension)) {
            case FREESTYLE:
                String freestyleUrlTemplate = freestyleReportUrlTemplate(encodedReportTitle);
                return new JobDetails(freestyleJobWithDetails(), freestyleUrlTemplate);
            case FOLDER:
                String folderUrlTemplate = folderReportUrlTemplate(encodedReportTitle);
                return new JobDetails(folderJobWithDetails(), folderUrlTemplate);
            default:
                throw new GradleException("Conflict between freestyle and folder jobs. " +
                        "Please, add one of freestyle or folder Jenkins job.");
        }
    }

    private JobWithDetails freestyleJobWithDetails() throws IOException {
        return getJenkinsServer().getJob(extension.freestyleJob.jobName);
    }

    private String freestyleReportUrlTemplate(@Nullable String encodedReportTitle) {
        if (isNullOrEmpty(encodedReportTitle)) {
            return null;
        }
        String freestylePattern = "%s/job/%s/%s/%s";
        String encodedJobName = encode(extension.freestyleJob.jobName);
        return String.format(freestylePattern, baseJenkinsUrl, encodedJobName, BUILD_ID_TOKEN, encodedReportTitle);
    }

    private JobWithDetails folderJobWithDetails() throws IOException {
        String jobName = extension.folderJob.jobName;
        String folderName = extension.folderJob.folderName;
        String folderUrl = baseJenkinsUrl + String.format("/job/%s/", folderName);
        return getJenkinsServer().getJob(new FolderJob(folderName, folderUrl), jobName);
    }

    private String folderReportUrlTemplate(@Nullable String encodedReportTitle) {
        if (isNullOrEmpty(encodedReportTitle)) {
            return null;
        }
        String folderPattern = "%s/job/%s/job/%s/%s/%s";
        String folderName = encode(extension.folderJob.folderName);
        String folderJobName = encode(extension.folderJob.jobName);
        return String.format(folderPattern, baseJenkinsUrl, folderName, folderJobName, BUILD_ID_TOKEN, encodedReportTitle);
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

    @Nullable
    private String encodedReportTitle() {
        String jenkinsReportTitle = extension.jenkinsReportTitle;
        if (isNullOrEmpty(jenkinsReportTitle)) {
            return null;
        }
        return encodeReportTitle(jenkinsReportTitle);
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

    private JobType getJobType(ForkJenkinsReportExtension extension) {
        if ((extension.freestyleJob == null && extension.folderJob == null) ||
                (extension.freestyleJob != null && extension.folderJob != null)) {
            return JobType.CONFLICTING;
        }

        if (extension.freestyleJob != null) {
            return JobType.FREESTYLE;
        }

        return JobType.FOLDER;
    }

    private enum JobType {
        FREESTYLE,
        FOLDER,
        CONFLICTING
    }
}
