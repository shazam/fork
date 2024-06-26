/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PoolTestRunnerFactory {
    private final DeviceTestRunnerFactory deviceTestRunnerFactory;

    public PoolTestRunnerFactory(DeviceTestRunnerFactory deviceTestRunnerFactory) {
        this.deviceTestRunnerFactory = deviceTestRunnerFactory;
    }

    public Runnable createPoolTestRunner(Pool pool,
                                         Collection<TestCaseEvent> testCases,
                                         ProgressReporter progressReporter) {

        int totalTests = testCases.size();
        progressReporter.addPoolProgress(pool, new PoolProgressTrackerImpl(totalTests));

        return new PoolTestRunner(
                deviceTestRunnerFactory,
                pool,
                new ConcurrentLinkedDeque<>(testCases),
                progressReporter);
    }
}
