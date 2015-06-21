/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/**
 * Gradle plugin for Fork.
 */
class ForkPlugin implements Plugin<Project> {

    /** Task name prefix. */
    private static final String TASK_PREFIX = "fork"

    @Override
    void apply(final Project project) {

        if (!project.plugins.findPlugin(AppPlugin) && !project.plugins.findPlugin(LibraryPlugin)) {
            throw new IllegalStateException("Android plugin is not found")
        }

        project.extensions.add "fork", ForkExtension

        def forkTask = project.task(TASK_PREFIX) {
            group = JavaBasePlugin.VERIFICATION_GROUP
            description = "Runs all the instrumentation test variations on all the connected devices"
        }

        BaseExtension android = project.android
        android.testVariants.all { TestVariant variant ->

            String taskName = "${TASK_PREFIX}${variant.name.capitalize()}"
            List<ForkRunTask> tasks = createTask(variant, project, "")
            tasks.each {
                it.configure {
                    description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
                }
            }

            forkTask.dependsOn tasks
        }

        project.tasks.addRule(patternString("fork")) { String ruleTaskName ->
            if (ruleTaskName.startsWith("fork")) {
                String suffix = lowercase(ruleTaskName - "fork")
                if (android.testVariants.find { suffix.startsWith(it.name) } != null) {
                    // variant specific, not our case
                    return
                }
                String size = suffix.toLowerCase(Locale.US)
                if (isValidSize(size)) {
                    def variantTaskNames = forkTask.taskDependencies.getDependencies(forkTask).collect() { it.name }
                    project.task(ruleTaskName, dependsOn: variantTaskNames.collect() { "${it}${size}" })
                }
            }
        }
    }

    private static boolean isValidSize(String size) {
        return size in ['small', 'medium', 'large']
    }

    private static String lowercase(final String s) {
        return s[0].toLowerCase(Locale.US) + s.substring(1)
    }

    private static String patternString(final String taskName) {
        return "Pattern: $taskName<TestSize>: run instrumentation tests of particular size"
    }

    private static List<ForkRunTask> createTask(final TestVariant variant, final Project project, final String suffix) {
        if (variant.outputs.size() > 1) {
            throw new UnsupportedOperationException("Fork plugin for gradle does not support abi/density splits for test apks")
        }
        ForkExtension config = project.fork
        return variant.testedVariant.outputs.collect { def projectOutput ->
            ForkRunTask task = project.tasks.create("${TASK_PREFIX}${projectOutput.name.capitalize()}${suffix}", ForkRunTask)
            task.configure {
                group = JavaBasePlugin.VERIFICATION_GROUP
                if (projectOutput instanceof ApkVariantOutput) {
                    applicationApk = projectOutput.outputFile
                } else {
                    applicationApk = variant.outputs[0].outputFile
                }
                instrumentationApk = variant.outputs[0].outputFile

                File outputBase = config.baseOutputDir
                if (!outputBase) {
                    outputBase = new File(project.buildDir, "fork")
                }
                output = new File(outputBase, projectOutput.dirName)
                testClassRegex = config.testClassRegex
                testPackage = config.testPackage
                ignoreFailures = config.ignoreFailures
                testOutputTimeout = config.testOutputTimeout
                fallbackToScreenshots = config.fallbackToScreenshots;

                dependsOn projectOutput.assemble, variant.assemble
            }
        } as List<ForkRunTask>
    }

}
