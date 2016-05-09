/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

import com.shazam.fork.CommonDefaults;
import com.shazam.fork.system.axmlparser.InstrumentationInfo;

import java.io.File;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.system.axmlparser.InstumentationInfoFactory.parseFromFile;

public class Configuration {

    private final File androidSdk;
    private final File applicationApk;
    private final File instrumentationApk;
    private final String applicationPackage;
    private final String instrumentationPackage;
    private final String testRunnerClass;
    private final File output;
    private final String testPackage;
    private final Pattern testClassPattern;
    private final String serial;

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
    public String getApplicationPackage() {
        return applicationPackage;
    }

    @Nonnull
    public String getInstrumentationPackage() {
        return instrumentationPackage;
    }

    @Nonnull
    public String getTestRunnerClass() {
        return testRunnerClass;
    }

    @Nonnull
    public File getOutput() {
        return output;
    }

    @Nonnull
    public String getTestPackage() {
        return testPackage;
    }

    @Nonnull
    public Pattern getTestClassPattern() {
        return testClassPattern;
    }

    @Nonnull
    public String getSerial() {
        return serial;
    }

    private Configuration(Builder builder) {
        androidSdk = builder.androidSdk;
        applicationApk = builder.applicationApk;
        instrumentationApk = builder.instrumentationApk;
        applicationPackage = builder.applicationPackage;
        instrumentationPackage = builder.instrumentationPackage;
        testRunnerClass = builder.testRunnerClass;
        output = builder.output;
        testPackage = builder.testPackage;
        testClassPattern = Pattern.compile(builder.testClassRegex);
        serial = builder.serial;
    }

    public static class Builder {
        private File androidSdk;
        private File applicationApk;
        private File instrumentationApk;
        private String applicationPackage;
        private String instrumentationPackage;
        private String testRunnerClass;
        private File output;
        private String testPackage;
        private String testClassRegex;
        private String serial;

        public static Builder configuration() {
            return new Builder();
        }

        public Builder withAndroidSdk(File androidSdk) {
            this.androidSdk = androidSdk;
            return this;
        }

        public Builder withApplicationApk(File applicationApk) {
            this.applicationApk = applicationApk;
            return this;
        }

        public Builder withInstrumentationApk(File instrumentationApk) {
            this.instrumentationApk = instrumentationApk;
            return this;
        }

        public Builder withApplicationPackage(String applicationPackage) {
            this.applicationPackage = applicationPackage;
            return this;
        }

        public Builder withInstrumentationPackage(String instrumentationPackage) {
            this.instrumentationPackage = instrumentationPackage;
            return this;
        }

        public Builder withTestRunnerClass(String testRunnerClass) {
            this.testRunnerClass = testRunnerClass;
            return this;
        }

        public Builder withOutput(File output) {
            this.output = output;
            return this;
        }

        public Builder withTestPackage(String testPackage) {
            this.testPackage = testPackage;
            return this;
        }

        public Builder withTestClassRegex(String testClassRegex) {
            this.testClassRegex = testClassRegex;
            return this;
        }

        public Builder withSerial(String serial) {
            this.serial = serial;
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

            InstrumentationInfo instrumentationInfo = parseFromFile(instrumentationApk);
            checkNotNull(instrumentationInfo.getApplicationPackage(), "Application package was not found in test APK");
            applicationPackage = instrumentationInfo.getApplicationPackage();
            checkNotNull(instrumentationInfo.getInstrumentationPackage(), "Instrumentation package was not found in test APK");
            instrumentationPackage = instrumentationInfo.getInstrumentationPackage();
            checkNotNull(instrumentationInfo.getTestRunnerClass(), "Test runner class was not found in test APK");
            testRunnerClass = instrumentationInfo.getTestRunnerClass();
            testClassRegex = assignValueOrDefaultIfNull(testClassRegex, CommonDefaults.TEST_CLASS_REGEX);
            testPackage = assignValueOrDefaultIfNull(testPackage, instrumentationInfo.getInstrumentationPackage());
            checkNotNull(serial, "Device serial is required.");

            return new Configuration(this);
        }

        private static <T> T assignValueOrDefaultIfNull(T value, T defaultValue) {
            return value != null ? value : defaultValue;
        }
    }
}
