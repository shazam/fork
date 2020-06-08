/*
 * Copyright 2019 Apple Inc.
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
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.tasks.PackageAndroidArtifact
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
            ChimpRunnerTask chimpRunnerTask = createTask(variant, project)
            chimprunnerTask.dependsOn chimpRunnerTask
        }
    }

    private static ChimpRunnerTask createTask(final TestVariant variant, final Project project) {
        if (variant.outputs.size() > 1) {
            throw new UnsupportedOperationException("The Chimprunner Gradle plugin for gradle does not support abi/density splits for test apks")
        }
        ChimpRunnerTask task = project.tasks.create("${TASK_PREFIX}${variant.name.capitalize()}", ChimpRunnerTask)

        variant.testedVariant.outputs.all { BaseVariantOutput baseVariantOutput ->
            checkTestedVariants(baseVariantOutput)
            task.configure {
                ChimpConfiguration config = project.chimprunner

                description = "Runs performance tests on a selected device for '${variant.name}' variation and generates a file containing reports"
                group = REPORTING_GROUP
                testClassRegex = config.testClassRegex
                testPackage = config.testPackage
                ignoreFailures = config.ignoreFailures
                serial = config.serial

                instrumentationApk = getApkFileFromPackageAndroidArtifact(variant)
                applicationApk = getApkFileFromPackageAndroidArtifact(variant.testedVariant as ApkVariant)

                String baseOutputDir = config.baseOutputDir
                File outputBase
                if (baseOutputDir) {
                    outputBase = new File(baseOutputDir);
                } else {
                    outputBase = new File(project.buildDir, "chimprunner")
                }
                output = new File(outputBase, baseVariantOutput.dirName)
                dependsOn variant.testedVariant.assembleProvider.name, variant.assembleProvider.name
            }
            task.outputs.upToDateWhen { false }
        }
        return task
    }

    private static File getApkFileFromPackageAndroidArtifact(ApkVariant variant) {
        PackageAndroidArtifact application = variant.packageApplicationProvider.get()
        return new File(application.outputDirectory, application.apkNames.first())
    }

    /**
     * Checks that if the base variant contains more than one outputs (and has therefore splits), it is the universal APK.
     * Otherwise, we can test the single output. This is a workaround until Fork supports test & app splits properly.
     *
     * @param baseVariant the tested variant
     */
    private static checkTestedVariants(BaseVariantOutput baseVariantOutput) {
        if (baseVariantOutput.outputs.size() > 1) {
            throw new UnsupportedOperationException(
                    "The Fork plugin does not support abi splits for app APKs, but supports testing via a universal APK. " +
                            "Add the flag \"universalApk true\" in the android.splits.abi configuration."
            )
        }
    }
}
