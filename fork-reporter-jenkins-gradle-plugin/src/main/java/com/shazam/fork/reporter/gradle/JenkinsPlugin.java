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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

import groovy.lang.Closure;

public class JenkinsPlugin implements Plugin<Project> {
    private static final String FORK_REPORTER_JENKINS_EXTENSION = "forkJenkinsReport";

    @Override
    public void apply(final Project project) {
        project.getExtensions().add(FORK_REPORTER_JENKINS_EXTENSION, ForkJenkinsReportExtension.class);

        ForkJenkinsReportTask forkJenkinsReport = project.getTasks().create("forkJenkinsReport", ForkJenkinsReportTask.class);
        forkJenkinsReport.configure(new Closure(forkJenkinsReport) {
            @Override
            public Object call() {
                ForkJenkinsReportTask forkJenkinsReportTask = (ForkJenkinsReportTask) getOwner();
                ForkJenkinsReportExtension extension = (ForkJenkinsReportExtension) project.getExtensions().getByName(FORK_REPORTER_JENKINS_EXTENSION);
                forkJenkinsReportTask.reportTitle = extension.reportTitle;
                forkJenkinsReportTask.jenkinsUrl = extension.jenkinsUrl;
                forkJenkinsReportTask.jenkinsUsername = extension.jenkinsUsername;
                forkJenkinsReportTask.jenkinsPassword = extension.jenkinsPassword;
                forkJenkinsReportTask.jenkinsJobName = extension.jenkinsJobName;
                forkJenkinsReportTask.input = new File(project.getBuildDir(), "forkFlakinessReportTemp");
                forkJenkinsReportTask.output = new File(project.getBuildDir(), "forkFlakinessReport");

                return forkJenkinsReportTask.dependsOn();
            }
        });
    }
}
