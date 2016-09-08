/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork;

import java.util.Collection;

import groovy.lang.Closure;

/**
 * Fork extension.
 */
public class ForkConfiguration {

    /**
     * Output directory for Fork report files. If empty, the default dir will be used.
     */
    public String baseOutputDir;

    /**
     * Ignore test failures flag.
     */
    public boolean ignoreFailures;

    /**
     * Enables code coverage.
     */
    public boolean isCoverageEnabled;

    /**
     * Regex determining the class names to consider when finding tests to run.
     */
    public String testClassRegex;

    /**
     * The title of the final report
     */
    public String title;

    /**
     * The subtitle of the final report
     */
    public String subtitle;

    /**
     * The package to consider when scanning for instrumentation tests to run.
     */
    public String testPackage;

    /**
     * Maximum time in milli-seconds between ADB output during a test. Prevents tests from getting stuck.
     */
    public int testOutputTimeout;

    /**
     * The size of the tests that will be executed with this run.
     */
    public String testSize;

    /**
     * The collection of serials that should be excluded from this test run
     */
    public Collection<String> excludedSerials;

    /**
     * Indicate that screenshots are allowed when videos are not supported.
     */
    public boolean fallbackToScreenshots;

    /**
     * Amount of re-executions of failing tests allowed.
     */
    public int totalAllowedRetryQuota;

    /**
     * Max number of time each testCase is attempted again before declaring it as a failure.
     */
    public int retryPerTestCaseQuota;

    /**
     * The strategy that will be used to calculate the grouping of devices to pools.
     */
    public PoolingStrategy poolingStrategy;

    /**
     * Indicate that in Marshmallow+ all the required runtime permissions are granted automatically.
     * Default is true.
     */
    public boolean autoGrantPermissions = true;

    /**
     * Tries to restart adb if there are no devices found.
     */
    public boolean restartAdbIfNoDevices;

    public void poolingStrategy(Closure<?> poolingStrategyClosure) {
        poolingStrategy = new PoolingStrategy();
        poolingStrategyClosure.setDelegate(poolingStrategy);
        poolingStrategyClosure.call();
    }
}
