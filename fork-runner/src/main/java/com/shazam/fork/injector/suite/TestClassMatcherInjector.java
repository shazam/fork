/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.injector.suite;

import com.shazam.fork.Configuration;
import com.shazam.fork.suite.PackageAndClassNameMatcher;
import com.shazam.fork.suite.TestClassMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

class TestClassMatcherInjector {
    private static final Logger log = LoggerFactory.getLogger(TestClassMatcherInjector.class);

    private TestClassMatcherInjector() {}

    static TestClassMatcher testClassMatcher() {
        Configuration configuration = configuration();
        log.info("Fork will try to find tests in {} from your instrumentation APK.", configuration.getTestPackage());
        Pattern testPackagePattern = compilePatternFor(configuration.getTestPackage());
        return new PackageAndClassNameMatcher(testPackagePattern, configuration.getTestClassPattern());
    }

    private static Pattern compilePatternFor(String packageString) {
        return Pattern.compile(packageString.replace(".", "\\.") + ".*");
    }
}
