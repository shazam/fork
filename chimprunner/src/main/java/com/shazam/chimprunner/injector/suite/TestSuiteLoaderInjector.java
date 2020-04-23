/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner.injector.suite;

import com.shazam.fork.suite.TestSuiteLoader;

import static com.shazam.chimprunner.injector.ConfigurationInjector.configuration;
import static com.shazam.chimprunner.injector.io.DexFileExtractorInjector.dexFileExtractor;
import static com.shazam.chimprunner.injector.suite.TestClassMatcherInjector.testClassMatcher;

public class TestSuiteLoaderInjector {

    private TestSuiteLoaderInjector() {
    }

    public static TestSuiteLoader testSuiteLoader() {
        return new TestSuiteLoader(configuration().getInstrumentationApk(), dexFileExtractor(), testClassMatcher());
    }
}
