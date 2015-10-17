/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork;

import com.shazam.fork.model.InstrumentationInfo;

import java.io.File;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.Utils.cleanFile;
import static com.shazam.fork.system.axmlparser.InstumentationInfoFactory.parseFromFile;

public class ForkBuilder {
    private File androidSdk = cleanFile(Defaults.ANDROID_SDK);
    private File applicationApk;
    private File instrumentationApk;
    private File output = cleanFile(Defaults.TEST_OUTPUT);
    private String testClassRegex = Defaults.TEST_CLASS_REGEX;
    private String testPackage; // Will default to test APK's package name as soon as it's known
    private int testOutputTimeout = Defaults.TEST_OUTPUT_TIMEOUT_MILLIS;
    private boolean fallbackToScreenshots = true;

    public static ForkBuilder aFork() {
        return new ForkBuilder();
    }

    /**
     * Path to the local Android SDK directory.
     * @param androidSdk the path of the Android SDK
     * @return the builder
     */
    public ForkBuilder withAndroidSdk(File androidSdk) {
        this.androidSdk = androidSdk;
        return this;
    }

    /**
     * Path to application APK.
     * @param apk the application APK file
     * @return the builder
     */
    public ForkBuilder withApplicationApk(File apk) {
        applicationApk = apk;
        return this;
    }

    /**
     * Path to the instrumentation APK.
     * @param apk the instrumentation APK file
     * @return the builder
     */
    public ForkBuilder withInstrumentationApk(File apk) {
        instrumentationApk = apk;
        return this;
    }

    /**
     * Path to output directory where reports will be saved.
     * @param output the directory where the fork reports and files are saved
     * @return the builder
     */
    public ForkBuilder withOutputDirectory(@Nullable File output) {
        if (output != null) {
            this.output = output;
        }
        return this;
    }

    /**
     * Regex {@link Pattern} determining the class names to consider when finding tests to run.
     * @param testClassRegex the regex to match FQN of tests
     * @return the builder
     */
    public ForkBuilder withTestClassRegex(@Nullable String testClassRegex) {
        if (testClassRegex != null) {
            this.testClassRegex = testClassRegex;
        }
        return this;
    }

    /**
     * Regex {@link Pattern} determining the packages to consider when finding tests to run.
     * @param testPackage the test package that will be scanned
     * @return the builder
     */
    public ForkBuilder withTestPackage(@Nullable String testPackage) {
        if (testPackage != null) {
            this.testPackage = testPackage;
        }
        return this;
    }

    /**
     * Maximum time between test output from ADB.
     * @param testOutputTimeout the period in millis
     * @return this builder
     */
    public ForkBuilder withTestOutputTimeout(int testOutputTimeout) {
        this.testOutputTimeout = testOutputTimeout;
        return this;
    }

    public ForkBuilder withFallbackToScreenshots(boolean fallbackToScreenshots) {
        this.fallbackToScreenshots = fallbackToScreenshots;
        return this;
    }

    public Fork build() {
        checkNotNull(androidSdk, "SDK is required.");
        checkArgument(androidSdk.exists(), "SDK directory does not exist.");
        checkNotNull(applicationApk, "Application APK is required.");
        checkArgument(applicationApk.exists(), "Application APK file does not exist.");
        checkNotNull(instrumentationApk, "Instrumentation APK is required.");
        checkArgument(instrumentationApk.exists(), "Instrumentation APK file does not exist.");
        checkNotNull(output, "Output path is required.");
        checkArgument(testOutputTimeout >= 0, "Timeout must be non-negative.");

        InstrumentationInfo instrumentationInfo = parseFromFile(instrumentationApk);
        String testPackage = configuredOrInstrumentationPackage(instrumentationInfo.getInstrumentationPackage());
        Configuration configuration = new Configuration(
                androidSdk,
                applicationApk,
                instrumentationApk,
                instrumentationInfo,
                output,
                Pattern.compile(testClassRegex),
                compilePatternFor(testPackage),
                testPackage,
                testOutputTimeout,
                fallbackToScreenshots
        );
        return new Fork(configuration);
    }

    private String configuredOrInstrumentationPackage(String instrumentationPackage) {
        if (testPackage != null) {
            return testPackage;
        }
        return instrumentationPackage;
    }

    private Pattern compilePatternFor(String packageString) {
        return Pattern.compile(packageString.replace(".", "\\.") + ".*");
    }
}
