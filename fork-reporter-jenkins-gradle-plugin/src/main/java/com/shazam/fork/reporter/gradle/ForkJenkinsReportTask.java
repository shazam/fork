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

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ForkJenkinsReportTask extends DefaultTask {

    String reportTitle;
    String jenkinsUrl;
    String jenkinsUsername;
    String jenkinsPassword;
    String jenkinsJobName;
    @InputDirectory File input;
    @OutputDirectory File output;

    @TaskAction
    public void runForkJenkins() {
        System.out.println("Runnign Fork's Jenkins extraction");
        try {
            JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), jenkinsUsername, jenkinsPassword);
            JobWithDetails job = jenkinsServer.getJob(jenkinsJobName);
            BuildWithDetails details = job.getBuilds().get(0).details();
            details.toString();
        } catch (URISyntaxException | IOException e) {
            throw new GradleException("Could not get reports from Jenkins Server", e);
        }
    }
}
