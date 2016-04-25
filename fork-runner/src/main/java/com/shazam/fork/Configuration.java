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
package com.shazam.fork;

import com.shazam.fork.model.InstrumentationInfo;

import java.io.File;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class Configuration {
    private final File androidSdk;
    private final File applicationApk;
    private final File instrumentationApk;
    private final InstrumentationInfo instrumentationInfo;
    private final File output;
    private final Pattern testClassPattern;
    private final Pattern testPackagePattern;
    private final String testPackage;
    private final int testOutputTimeout;
    private final boolean fallbackToScreenshots;
    private final int totalAllowedRetryQuota;
    private final int retryPerTestCaseQuota;
    private final boolean isCoverageEnabled;
    private final boolean autoGrantRuntimePermissions;

    public Configuration(@Nonnull File androidSdk,
                         @Nonnull File applicationApk,
                         @Nonnull File instrumentationApk,
                         @Nonnull InstrumentationInfo instrumentationInfo,
                         @Nonnull File output,
                         @Nonnull Pattern testClassPattern,
                         @Nonnull Pattern testPackagePattern,
                         @Nonnull String testPackage,
                         int testOutputTimeout,
                         boolean fallbackToScreenshots,
                         int totalAllowedRetryQuota,
                         int retryPerTestCaseQuota,
                         boolean isCoverageEnabled, 
                         boolean autoGrantRuntimePermissions) {
        this.androidSdk = androidSdk;
        this.applicationApk = applicationApk;
        this.instrumentationApk = instrumentationApk;
        this.instrumentationInfo = instrumentationInfo;
        this.output = output;
        this.testClassPattern = testClassPattern;
        this.testPackagePattern = testPackagePattern;
        this.testPackage = testPackage;
        this.testOutputTimeout = testOutputTimeout;
        this.fallbackToScreenshots = fallbackToScreenshots;
        this.totalAllowedRetryQuota = totalAllowedRetryQuota;
        this.retryPerTestCaseQuota = retryPerTestCaseQuota;
        this.isCoverageEnabled = isCoverageEnabled;
        this.autoGrantRuntimePermissions = autoGrantRuntimePermissions;
    }

    @Nonnull
    public File getAndroidSdk() {
        return androidSdk;
    }

    @Nonnull
    public File getApplicationApk() {
        return applicationApk;
    }

    @Nonnull
    public File getInstrumentationApk() {
        return instrumentationApk;
    }

    @Nonnull
    public InstrumentationInfo getInstrumentationInfo() {
        return instrumentationInfo;
    }

    @Nonnull
    public File getOutput() {
        return output;
    }

    @Nonnull
    public Pattern getTestClassPattern() {
        return testClassPattern;
    }

    @Nonnull
    public Pattern getTestPackagePattern() {
        return testPackagePattern;
    }

    public String getTestPackage() {
        return testPackage;
    }

    public int getTestOutputTimeout() {
        return testOutputTimeout;
    }

    public boolean canFallbackToScreenshots() {
        return fallbackToScreenshots;
    }

    public int getTotalAllowedRetryQuota() {
        return totalAllowedRetryQuota;
    }

    public int getRetryPerTestCaseQuota() {
        return retryPerTestCaseQuota;
    }

    public boolean isCoverageEnabled() {
        return isCoverageEnabled;
    }

    public boolean autoGrantRuntimePermissions() {
        return autoGrantRuntimePermissions;
    }
}
