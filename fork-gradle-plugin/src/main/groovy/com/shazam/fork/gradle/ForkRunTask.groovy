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

import com.shazam.fork.Configuration
import com.shazam.fork.Fork
import com.shazam.fork.PoolingStrategy
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.shazam.fork.Configuration.Builder.configuration

/**
 * Task for using Fork.
 */
class ForkRunTask extends DefaultTask implements VerificationTask {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ForkRunTask.class)

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

    String title

    String subtitle

    String testClassRegex

    String testPackage

    boolean isCoverageEnabled

    int testOutputTimeout

    String testSize

    Collection<String> excludedSerials

    boolean fallbackToScreenshots

    int totalAllowedRetryQuota

    int retryPerTestCaseQuota

    PoolingStrategy poolingStrategy

    boolean autoGrantPermissions;

    boolean restartAdbIfNoDevices

    @TaskAction
    void runFork() {
        LOG.info("Run instrumentation tests $instrumentationApk for app $applicationApk")
        LOG.debug("Output: $output")
        LOG.debug("Ignore failures: $ignoreFailures")

        Configuration configuration = configuration()
                .withAndroidSdk(project.android.sdkDirectory)
                .withApplicationApk(applicationApk)
                .withInstrumentationApk(instrumentationApk)
                .withOutput(output)
                .withTitle(title)
                .withSubtitle(subtitle)
                .withTestClassRegex(testClassRegex)
                .withTestPackage(testPackage)
                .withTestOutputTimeout(testOutputTimeout)
                .withTestSize(testSize)
                .withExcludedSerials(excludedSerials)
                .withFallbackToScreenshots(fallbackToScreenshots)
                .withTotalAllowedRetryQuota(totalAllowedRetryQuota)
                .withRetryPerTestCaseQuota(retryPerTestCaseQuota)
                .withCoverageEnabled(isCoverageEnabled)
                .withPoolingStrategy(poolingStrategy)
                .withAutoGrantPermissions(autoGrantPermissions)
                .withRestartAdbIfNoDevices(restartAdbIfNoDevices)
                .build();

        boolean success = new Fork(configuration).run()
        if (!success && !ignoreFailures) {
            throw new GradleException("Tests failed! See ${output}/html/index.html")
        }
    }
}
