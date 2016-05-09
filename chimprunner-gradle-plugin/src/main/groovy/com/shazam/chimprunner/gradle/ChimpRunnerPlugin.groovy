/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.chimprunner.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import com.shazam.chimprunner.ChimpConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class ChimpRunnerPlugin implements Plugin<Project> {

    private static final String TASK_PREFIX = "chimprunner"
    public static final String REPORTING_GROUP = "Reporting"

    @Override
    void apply(final Project project) {
        if (!project.plugins.findPlugin(AppPlugin) && !project.plugins.findPlugin(LibraryPlugin)) {
            throw new IllegalStateException("Android plugin is not found")
        }

        project.extensions.add "chimprunner", ChimpConfiguration

        def chimprunnerTask = project.task(TASK_PREFIX) {
            group = REPORTING_GROUP
            description = "Runs performance tests on a selected device"
        }

        BaseExtension android = project.android
        android.testVariants.all { TestVariant variant ->
            List<ChimpRunnerTask> tasks = createTask(variant, project)
            tasks.each {
                it.configure {
                    description = "Runs performance tests on a selected device for '${variant.name}' variation and generates a file containing reports"
                }
            }

            chimprunnerTask.dependsOn tasks
        }
    }

    private static List<ChimpRunnerTask> createTask(final TestVariant variant, final Project project) {
        if (variant.outputs.size() > 1) {
            throw new UnsupportedOperationException("The Chimprunner Gradle plugin for gradle does not support abi/density splits for test apks")
        }
        ChimpConfiguration config = project.chimprunner
        return variant.testedVariant.outputs.collect { def projectOutput ->
            ChimpRunnerTask task = project.tasks.create("${TASK_PREFIX}${variant.name.capitalize()}", ChimpRunnerTask)
            task.configure {
                group = REPORTING_GROUP
                if (projectOutput instanceof ApkVariantOutput) {
                    applicationApk = projectOutput.outputFile
                } else {
                    applicationApk = variant.outputs[0].outputFile
                }
                instrumentationApk = variant.outputs[0].outputFile

                String baseOutputDir = config.baseOutputDir
                File outputBase
                if (baseOutputDir) {
                    outputBase = new File(baseOutputDir);
                } else {
                    outputBase = new File(project.buildDir, "chimprunner")
                }
                output = new File(outputBase, projectOutput.dirName)
                testClassRegex = config.testClassRegex
                testPackage = config.testPackage
                ignoreFailures = config.ignoreFailures
                serial = config.serial

                dependsOn projectOutput.assemble, variant.assemble
            }
            task.outputs.upToDateWhen { false }
            return task
        } as List<ChimpRunnerTask>
    }

}
