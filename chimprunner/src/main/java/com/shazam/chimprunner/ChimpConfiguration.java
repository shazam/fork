/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

/**
 * The configuration object that will used by Chimp.
 */
public class ChimpConfiguration {

    /**
     * Output directory for Chimp's report files. If empty, the default dir will be used.
     */
    public String baseOutputDir;

    /**
     * Ignore test failures flag.
     */
    public boolean ignoreFailures;

    /**
     * The package to consider when scanning for instrumentation tests to run.
     */
    public String testPackage;

    /**
     * Regex determining the class names to consider when finding tests to run.
     */
    public String testClassRegex;

    /**
     * The serial of the device that the tests will run on.
     */
    public String serial;
}
