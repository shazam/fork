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

import com.shazam.chimprunner.ChimpRunner
import com.shazam.chimprunner.Configuration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Task for running Chimprunner.
 */
class ChimpRunnerTask extends DefaultTask implements VerificationTask {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ChimpRunnerTask.class)

    /** If true then test failures do not cause a build failure. */
    boolean ignoreFailures

    /** Instrumentation APK. */
    @InputFile
    File instrumentationApk

    /** Application APK. */
    @InputFile
    File applicationApk

    /** Output directory. */
    @OutputDirectory
    File output

    String testClassRegex
    String testPackage
    String serial;

    @TaskAction
    void runChimpRunner() {
        LOG.info("Run performance tests $instrumentationApk for app $applicationApk")
        LOG.debug("Output: $output")
        LOG.debug("Ignore failures: $ignoreFailures")

        Configuration configuration = Configuration.Builder.configuration()
                .withAndroidSdk(project.android.sdkDirectory)
                .withApplicationApk(applicationApk)
                .withInstrumentationApk(instrumentationApk)
                .withOutput(output)
                .withTestPackage(testPackage)
                .withTestClassRegex(testClassRegex)
                .withSerial(serial)
                .build();

        boolean success = new ChimpRunner(configuration).run()
        if (!success && !ignoreFailures) {
            throw new GradleException("Tests failed! See ${output}/html/index.html")
        }
    }
}
