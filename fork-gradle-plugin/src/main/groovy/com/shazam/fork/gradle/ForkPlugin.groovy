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
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.shazam.fork.ForkConfiguration
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

        project.extensions.add "fork", ForkConfiguration

        def forkTask = project.task(TASK_PREFIX) {
            group = JavaBasePlugin.VERIFICATION_GROUP
            description = "Runs all the instrumentation test variations on all the connected devices"
        }

        BaseExtension android = project.android
        android.testVariants.all { TestVariant variant ->
            ForkRunTask forkTaskForTestVariant = createTask(variant, project)
            forkTask.dependsOn forkTaskForTestVariant
        }
    }

    private static ForkRunTask createTask(final TestVariant variant, final Project project) {
        checkTestVariants(variant)
        checkTestedVariants(variant.testedVariant)

        def forkTask = project.tasks.create("${TASK_PREFIX}${variant.name.capitalize()}", ForkRunTask)

        forkTask.configure {
            ForkConfiguration config = project.fork

            description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
            group = JavaBasePlugin.VERIFICATION_GROUP

            def firstTestedVariantOutput = variant.testedVariant.outputs.get(0)
            applicationApk = firstTestedVariantOutput.outputFile
            instrumentationApk = variant.outputs.get(0).outputFile

            String baseOutputDir = config.baseOutputDir
            File outputBase
            if (baseOutputDir) {
                outputBase = new File(baseOutputDir)
            } else {
                outputBase = new File(project.buildDir, "fork")
            }
            output = new File(outputBase, firstTestedVariantOutput.dirName)
            title = config.title
            subtitle = config.subtitle
            testClassRegex = config.testClassRegex
            testPackage = config.testPackage
            testOutputTimeout = config.testOutputTimeout
            testSize = config.testSize
            excludedSerials = config.excludedSerials
            fallbackToScreenshots = config.fallbackToScreenshots
            totalAllowedRetryQuota = config.totalAllowedRetryQuota
            retryPerTestCaseQuota = config.retryPerTestCaseQuota
            isCoverageEnabled = config.isCoverageEnabled
            poolingStrategy = config.poolingStrategy
            autoGrantPermissions = config.autoGrantPermissions
            ignoreFailures = config.ignoreFailures
            restartAdbIfNoDevices = config.restartAdbIfNoDevices

            dependsOn firstTestedVariantOutput.assemble, variant.assemble
        }
        forkTask.outputs.upToDateWhen { false }
        return forkTask
    }

    private static checkTestVariants(TestVariant testVariant) {
        if (testVariant.outputs.size() > 1) {
            throw new UnsupportedOperationException("The Fork plugin does not support abi/density splits for test APKs")
        }
    }

    /**
     * Checks that if the base variant contains more than one outputs (and has therefore splits), it is the universal APK.
     * Otherwise, we can test the single output. This is a workaround until Fork supports test & app splits properly.
     *
     * @param baseVariant the tested variant
     */
    private static checkTestedVariants(BaseVariant baseVariant) {
        def outputFile = baseVariant.outputs.get(0).outputFile
        if (baseVariant.outputs.size() > 1) {
            if (outputFile.toString().contains("universal")) {
                return outputFile
            }
            throw new UnsupportedOperationException(
                    "The Fork plugin does not support abi splits for app APKs, but supports testing via a universal APK. " +
                    "Add the flag \"universalApk true\" in the android.splits.abi configuration."
            )
        } else {
            return outputFile
        }
    }
}
