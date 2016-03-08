/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.Configuration;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.io.FileManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PoolTestRunnerFactory {
    private final Configuration configuration;
    private final FileManager fileManager;
    private final DeviceTestRunnerFactory deviceTestRunnerFactory;

    public PoolTestRunnerFactory(Configuration configuration,
                                 FileManager fileManager,
                                 DeviceTestRunnerFactory deviceTestRunnerFactory) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        this.deviceTestRunnerFactory = deviceTestRunnerFactory;
    }

    public PoolTestRunner createPoolTestRunner(Pool pool,
                                               List<TestCaseEvent> testCases,
                                               CountDownLatch poolCountDownLatch,
                                               ProgressReporter progressReporter,
                                               FailureAccumulator failureAccumulator) {

        int totalTests = testCases.size();
        progressReporter.addPoolProgress(pool, new PoolProgressTracker(totalTests));

        return new PoolTestRunner(
                configuration,
                fileManager,
                deviceTestRunnerFactory,
                pool,
                new LinkedList<>(testCases),
                poolCountDownLatch,
                progressReporter,
                failureAccumulator);
    }

  /*  private int countTests(List<TestClass> testClasses) {
        int sum = 0;
        for (TestClass testClass : testClasses) {
            sum += testClass.getMethods().size();
        }
        return sum;
    }*/
}
