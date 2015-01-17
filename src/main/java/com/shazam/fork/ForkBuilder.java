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

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.model.InstrumentationInfo.parseFromFile;

public class ForkBuilder {

    /** Build a test suite for the specified devices and output. */
    private File androidSdk;
    private File applicationApk;
    private File instrumentationApk;
    private File output;
    private int idleTimeout = 2 * 60 * 1000; // Empirical default.
    private int testTimeout = 4 * 60 * 1000; // Empirical default.
    private int testIntervalTimeout = 30 * 1000; // Empirical default.

    public static ForkBuilder aFork() {
        return new ForkBuilder();
    }

    /**
     * Path to the local Android SDK directory.
     * @param androidSdk android SDK location
     * @return this builder
     */
    public ForkBuilder withAndroidSdk(File androidSdk) {
        this.androidSdk = androidSdk;
        return this;
    }

    /**
     * Path to application APK.
     * @param apk the location of the production APK
     * @return this builder
     */
    public ForkBuilder withApplicationApk(File apk) {
        applicationApk = apk;
        return this;
    }

    /**
     * Path to the instrumentation APK.
     * @param apk the location of the instrumentation APK
     * @return this builder
     */
    public ForkBuilder withInstrumentationApk(File apk) {
        instrumentationApk = apk;
        return this;
    }

    /**
     * Path to output directory.
     * @param output the output directory
     * @return this builder
     */
    public ForkBuilder withOutputDirectory(File output) {
        this.output = output;
        return this;
    }

    /**
     * Maximum inactivity of a device
     * @param idleTimeout the period in millis
     * @return this builder
     */
    public ForkBuilder withIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Maximum time a test can take
     * @param testTimeout the period in millis
     * @return this builder
     */
    public ForkBuilder withTestTimeout(int testTimeout) {
        this.testTimeout = testTimeout;
        return this;
    }

    /**
     * Millis for maximum time between two tests
     * @param testIntervalTimeout the period in millis
     * @return this builder
     */
    public ForkBuilder withTestIntervalTimeout(int testIntervalTimeout) {
        this.testIntervalTimeout = testIntervalTimeout;
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

        Configuration configuration = new Configuration(
                androidSdk,
                applicationApk,
                instrumentationApk,
                parseFromFile(instrumentationApk),
                output,
                idleTimeout,
                testTimeout,
                testIntervalTimeout);
        return new Fork(configuration);
    }

}
