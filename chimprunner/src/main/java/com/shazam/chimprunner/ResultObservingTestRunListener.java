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

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;

import java.util.Map;

class ResultObservingTestRunListener implements ITestRunListener {
    private TestIdentifier testIdentifierOfFailedTest;

    @Override
    public void testRunStarted(String runName, int testCount) {

    }

    @Override
    public void testStarted(TestIdentifier test) {

    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        testIdentifierOfFailedTest = test;
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {

    }

    @Override
    public void testIgnored(TestIdentifier test) {

    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {

    }

    @Override
    public void testRunFailed(String errorMessage) {

    }

    @Override
    public void testRunStopped(long elapsedTime) {

    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {

    }

    public boolean hasFailed() {
        return testIdentifierOfFailedTest != null;
    }

    public TestIdentifier getTestFailure() {
        return testIdentifierOfFailedTest;
    }
}
