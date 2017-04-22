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
package com.metova.whisk.gradle

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
 * Gradle plugin for Whisk.
 */
class WhiskPlugin implements Plugin<Project> {

    /** Task name prefix. */
    private static final String TASK_PREFIX = "whisk"

    @Override
    void apply(final Project project) {

        if (!project.plugins.findPlugin(AppPlugin) && !project.plugins.findPlugin(LibraryPlugin)) {
            throw new IllegalStateException("Android plugin is not found")
        }

        project.extensions.add "whisk", ForkConfiguration

        def whiskTask = project.task(TASK_PREFIX) {
            group = JavaBasePlugin.VERIFICATION_GROUP
            description = "Runs all the instrumentation test variations on all the connected devices"
        }

        BaseExtension android = project.android
        android.testVariants.all { TestVariant variant ->
            WhiskRunTask whiskTaskForTestVariant = createTask(variant, project)
            whiskTask.dependsOn whiskTaskForTestVariant
        }
    }

    private static WhiskRunTask createTask(final TestVariant variant, final Project project) {
        checkTestVariants(variant)
        checkTestedVariants(variant.testedVariant)

        def whiskTask = project.tasks.create("${TASK_PREFIX}${variant.name.capitalize()}", WhiskRunTask)

        whiskTask.configure {
            ForkConfiguration config = project.whisk

            description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
            group = JavaBasePlugin.VERIFICATION_GROUP

            def firstTestedVariantOutput = variant.testedVariant.outputs.get(0)
            applicationApk = firstTestedVariantOutput.outputFile
            instrumentationApk = variant.outputs.get(0).outputFile
            //If we are testing a library, the app apk must be the same than the instrumentation apk
            if (applicationApk.path.endsWith("aar")) {
                applicationApk = instrumentationApk
            }

            String baseOutputDir = config.baseOutputDir
            File outputBase
            if (baseOutputDir) {
                outputBase = new File(baseOutputDir)
            } else {
                outputBase = new File(project.buildDir, "whisk")
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
            excludedAnnotation = config.excludedAnnotation
            screenRecording = config.screenRecording

            dependsOn firstTestedVariantOutput.assemble, variant.assemble
        }
        whiskTask.outputs.upToDateWhen { false }
        return whiskTask
    }

    private static checkTestVariants(TestVariant testVariant) {
        if (testVariant.outputs.size() > 1) {
            throw new UnsupportedOperationException("The Whisk plugin does not support abi/density splits for test APKs")
        }
    }

    /**
     * Checks that if the base variant contains more than one outputs (and has therefore splits), it is the universal APK.
     * Otherwise, we can test the single output. This is a workaround until Whisk supports test & app splits properly.
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
                    "The Whisk plugin does not support abi splits for app APKs, but supports testing via a universal APK. " +
                    "Add the flag \"universalApk true\" in the android.splits.abi configuration."
            )
        } else {
            return outputFile
        }
    }
}
