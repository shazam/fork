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

import java.util.regex.Pattern

/**
 * Fork extension.
 */
class ForkExtension {

    /**
     * Output directory for Fork report files. If empty, the default dir will be used.
     */
    File baseOutputDir

    /**
     * Ignore test failures flag.
     */
    boolean ignoreFailures

    /**
     * Regex determining the class names to consider when finding tests to run.
     */
    String testClassRegex

    /**
     * The package to consider when scanning for instrumentation tests to run.
     */
    String testPackage

    /**
     * Maximum time in milli-seconds between ADB output during a test. Prevents tests from getting stuck.
     */
    int testOutputTimeout

    /**
     * Indicate that screenshots are allowed when videos are not supported.
     */
    boolean fallbackToScreenshots
}
