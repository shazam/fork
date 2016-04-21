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

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.model.InstrumentationInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.system.axmlparser.InstumentationInfoFactory.parseFromFile;
import static java.util.regex.Pattern.compile;

public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final File androidSdk;
    private final File applicationApk;
    private final File instrumentationApk;
    private final InstrumentationInfo instrumentationInfo;
    private final File output;
    private final String title;
    private final String subtitle;
    private final Pattern testClassPattern;
    private final String testPackage;
    private final int testOutputTimeout;
    private final IRemoteAndroidTestRunner.TestSize testSize;
    private final boolean fallbackToScreenshots;
    private final int totalAllowedRetryQuota;
    private final int retryPerTestCaseQuota;
    private final boolean isCoverageEnabled;

    private Configuration(Builder builder) {
        androidSdk = builder.androidSdk;
        applicationApk = builder.applicationApk;
        instrumentationApk = builder.instrumentationApk;
        instrumentationInfo = builder.instrumentationInfo;
        output = builder.output;
        title = builder.title;
        subtitle = builder.subtitle;
        testClassPattern = builder.testClassPattern;
        testPackage = builder.testPackage;
        testOutputTimeout = builder.testOutputTimeout;
        testSize = builder.testSize;
        fallbackToScreenshots = builder.fallbackToScreenshots;
        totalAllowedRetryQuota = builder.totalAllowedRetryQuota;
        retryPerTestCaseQuota = builder.retryPerTestCaseQuota;
        isCoverageEnabled = builder.isCoverageEnabled;
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
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getSubtitle() {
        return subtitle;
    }

    @Nonnull
    public Pattern getTestClassPattern() {
        return testClassPattern;
    }

    @Nonnull
    public String getTestPackage() {
        return testPackage;
    }

    public int getTestOutputTimeout() {
        return testOutputTimeout;
    }

    @Nullable
    public IRemoteAndroidTestRunner.TestSize getTestSize() {
        return testSize;
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

    public static class Builder {
        private File androidSdk;
        private File applicationApk;
        private File instrumentationApk;
        private InstrumentationInfo instrumentationInfo;
        private File output;
        private String title = "Fork Report";
        private String subtitle = "";
        private Pattern testClassPattern = compile(Defaults.TEST_CLASS_REGEX);
        private String testPackage;
        private int testOutputTimeout = Defaults.TEST_OUTPUT_TIMEOUT_MILLIS;
        private IRemoteAndroidTestRunner.TestSize testSize;
        private boolean fallbackToScreenshots;
        private int totalAllowedRetryQuota = 0;
        private int retryPerTestCaseQuota = 1;
        private boolean isCoverageEnabled;

        public static Builder configuration() {
            return new Builder();
        }

        public Builder withAndroidSdk(@Nonnull File androidSdk) {
            this.androidSdk = androidSdk;
            return this;
        }

        public Builder withApplicationApk(@Nonnull File applicationApk) {
            this.applicationApk = applicationApk;
            return this;
        }

        public Builder withInstrumentationApk(@Nonnull File instrumentationApk) {
            this.instrumentationApk = instrumentationApk;
            return this;
        }

        public Builder withOutput(@Nonnull File output) {
            this.output = output;
            return this;
        }

        public Builder withTitle(@Nonnull String title) {
            this.title = title;
            return this;
        }

        public Builder withSubtitle(@Nonnull String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder withTestClassPattern(@Nonnull Pattern testClassPattern) {
            this.testClassPattern = testClassPattern;
            return this;
        }

        public Builder withTestPackage(@Nullable String testPackage) {
            this.testPackage = testPackage;
            return this;
        }

        public Builder withTestOutputTimeout(int testOutputTimeout) {
            this.testOutputTimeout = testOutputTimeout;
            return this;
        }

        public Builder withTestSize(@Nullable String testSize) {
            this.testSize = IRemoteAndroidTestRunner.TestSize.getTestSize(testSize);
            return this;
        }

        public Builder withFallbackToScreenshots(boolean fallbackToScreenshots) {
            this.fallbackToScreenshots = fallbackToScreenshots;
            return this;
        }

        public Builder withTotalAllowedRetryQuota(int totalAllowedRetryQuota) {
            this.totalAllowedRetryQuota = totalAllowedRetryQuota;
            return this;
        }

        public Builder withRetryPerTestCaseQuota(int retryPerTestCaseQuota) {
            this.retryPerTestCaseQuota = retryPerTestCaseQuota;
            return this;
        }

        public Builder withIsCoverageEnabled(boolean isCoverageEnabled) {
            this.isCoverageEnabled = isCoverageEnabled;
            return this;
        }

        public Configuration build() {
            checkNotNull(androidSdk, "SDK is required.");
            checkArgument(androidSdk.exists(), "SDK directory does not exist.");
            checkNotNull(applicationApk, "Application APK is required.");
            checkArgument(applicationApk.exists(), "Application APK file does not exist.");
            checkNotNull(instrumentationApk, "Instrumentation APK is required.");
            checkArgument(instrumentationApk.exists(), "Instrumentation APK file does not exist.");
            checkNotNull(output, "Output path is required.");
            checkArgument(testOutputTimeout >= 0, "Timeout must be non-negative.");
            checkArgument(totalAllowedRetryQuota >= 0, "Total allowed retry quota should not be negative.");
            checkArgument(retryPerTestCaseQuota >= 0, "Retry per test case quota should not be negative.");
            logArgumentsBadInteractions();

            instrumentationInfo = parseFromFile(instrumentationApk);
            testPackage = configuredOrInstrumentationPackage(instrumentationInfo.getInstrumentationPackage());
            return new Configuration(this);
        }

        private void logArgumentsBadInteractions() {
            if(totalAllowedRetryQuota > 0 && totalAllowedRetryQuota < retryPerTestCaseQuota){
                logger.warn("Total allowed retry quota ["+ totalAllowedRetryQuota +"] " +
                        "is smaller than Retry per test case quota ["+retryPerTestCaseQuota+"]. " +
                        "This is suspicious as the fist mentioned parameter is an overall cap.");
            }
        }

        private String configuredOrInstrumentationPackage(String instrumentationPackage) {
            if (testPackage != null) {
                return testPackage;
            }
            return instrumentationPackage;
        }
    }
}
