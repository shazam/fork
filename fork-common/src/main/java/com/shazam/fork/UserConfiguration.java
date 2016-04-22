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

/**
 * Fork extension.
 */
public class UserConfiguration {

    /**
     * Output directory for Fork report files. If empty, the default dir will be used.
     */
    String baseOutputDir;

    /**
     * Ignore test failures flag.
     */
    boolean ignoreFailures;

    /**
     * Enables code coverage.
     */
    boolean isCoverageEnabled;

    /**
     * Regex determining the class names to consider when finding tests to run.
     */
    String testClassRegex;

    /**
     * The title of the final report
     */
    String title;

    /**
     * The subtitle of the final report
     */
    String subtitle;

    /**
     * The package to consider when scanning for instrumentation tests to run.
     */
    String testPackage;

    /**
     * Maximum time in milli-seconds between ADB output during a test. Prevents tests from getting stuck.
     */
    int testOutputTimeout;

    /**
     * The size of the tests that will be executed with this run.
     */
    String testSize;

    /**
     * The collection of serials that should be excluded from this test run
     */
    Collection<String> excludedSerials;

    /**
     * Indicate that screenshots are allowed when videos are not supported.
     */
    boolean fallbackToScreenshots;

    /**
     * Amount of re-executions of failing tests allowed.
     */
    int totalAllowedRetryQuota;

    /**
     * Max number of time each testCase is attempted again before declaring it as a failure.
     */
    int retryPerTestCaseQuota;

    /**
     * The strategy that will be used to calculate the grouping of devices to pools.
     */
    PoolingStrategy poolingStrategy;
}
