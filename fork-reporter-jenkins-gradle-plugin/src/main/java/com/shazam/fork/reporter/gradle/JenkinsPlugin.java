/*
 * Copyright 2019 Apple Inc.
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

public class JenkinsPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getExtensions().create("forkJenkins", ForkJenkinsReportExtension.class);
        project.getTasks().create("forkJenkinsReport", ForkJenkinsReportTask.class,
                forkJenkinsReportTask -> {
                    forkJenkinsReportTask.setGroup("Reporting");
                    forkJenkinsReportTask.setDescription("Creates a report of test flakiness, based on the history of Fork test runs");
                });
    }
}
