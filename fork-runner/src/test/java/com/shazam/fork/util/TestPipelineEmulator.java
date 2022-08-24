/*
 * Copyright 2022 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shazam.fork.util;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class TestPipelineEmulator {
    private final Map<TestIdentifier, String> failingTests;
    private final String fatalErrorMessage;

    private TestPipelineEmulator(Builder builder) {
        this.failingTests = builder.failingTests;
        this.fatalErrorMessage = builder.fatalErrorMessage;
    }

    public void emulateFor(ITestRunListener testRunListener, TestIdentifier test) {
        emulateFor(testRunListener, Collections.singleton(test));
    }

    public void emulateFor(ITestRunListener testRunListener, Iterable<TestIdentifier> tests) {
        testRunListener.testRunStarted("emulated", 1);

        for (TestIdentifier test : tests) {
            testRunListener.testStarted(test);
            String trace = failingTests.get(test);
            if (trace != null) {
                testRunListener.testFailed(test, trace);
            }
            testRunListener.testEnded(test, emptyMap());
        }

        if (fatalErrorMessage != null) {
            testRunListener.testRunFailed(fatalErrorMessage);
        }
        testRunListener.testRunEnded(100L, emptyMap());
    }

    public static class Builder {
        private final Map<TestIdentifier, String> failingTests = new HashMap<>();
        private String fatalErrorMessage;

        public static Builder testPipelineEmulator() {
            return new Builder();
        }

        public Builder withTestFailed(TestIdentifier failingTest, String trace) {
            failingTests.put(failingTest, trace);
            return this;
        }

        public Builder withTestRunFailed(String fatalErrorMessage) {
            this.fatalErrorMessage = fatalErrorMessage;
            return this;
        }

        public TestPipelineEmulator build() {
            return new TestPipelineEmulator(this);
        }
    }
}
