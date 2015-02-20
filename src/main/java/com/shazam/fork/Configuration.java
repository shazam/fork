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

public class Configuration {
    private final File androidSdk;
    private final File applicationApk;
    private final File instrumentationApk;
    private final InstrumentationInfo instrumentationInfo;
    private final File output;
    private final Pattern testClassPattern;
    private final Pattern testPackagePattern;
    private final int idleTimeout;
    private final int testTimeout;
    private final int testIntervalTimeout;

    Configuration(File androidSdk, File applicationApk, File instrumentationApk,
                  InstrumentationInfo instrumentationInfo, File output, Pattern testClassPattern,
                  Pattern testPackagePattern, int idleTimeout, int testTimeout, int testIntervalTimeout) {
        this.androidSdk = androidSdk;
        this.applicationApk = applicationApk;
        this.instrumentationApk = instrumentationApk;
        this.instrumentationInfo = instrumentationInfo;
        this.output = output;
        this.testClassPattern = testClassPattern;
        this.testPackagePattern = testPackagePattern;
        this.idleTimeout = idleTimeout;
        this.testTimeout = testTimeout;
        this.testIntervalTimeout = testIntervalTimeout;
    }

    public File getAndroidSdk() {
        return androidSdk;
    }

    public File getApplicationApk() {
        return applicationApk;
    }

    public File getInstrumentationApk() {
        return instrumentationApk;
    }

    public InstrumentationInfo getInstrumentationInfo() {
        return instrumentationInfo;
    }

    public File getOutput() {
        return output;
    }

    public Pattern getTestClassPattern() {
        return testClassPattern;
    }

    public Pattern getTestPackagePattern() {
        return testPackagePattern;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public int getTestTimeout() {
        return testTimeout;
    }

    public int getTestIntervalTimeout() {
        return testIntervalTimeout;
    }
}
