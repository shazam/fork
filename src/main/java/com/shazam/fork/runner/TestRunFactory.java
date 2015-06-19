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

import com.android.ddmlib.testrunner.ITestRunListener;
import com.shazam.fork.Configuration;
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.listeners.*;
import com.shazam.fork.model.*;

import java.util.List;

import static com.shazam.fork.model.TestRunParameters.Builder.testRunParameters;

public class TestRunFactory {

    private final Configuration configuration;
    private final RuntimeConfiguration runtimeConfiguration;
    private final TestRunListenersFactory testRunListenersFactory;

    public TestRunFactory(Configuration configuration,
                          RuntimeConfiguration runtimeConfiguration,
                          TestRunListenersFactory testRunListenersFactory) {
        this.configuration = configuration;
        this.runtimeConfiguration = runtimeConfiguration;
        this.testRunListenersFactory = testRunListenersFactory;
    }

    public TestRun createTestRun(TestClass testClass, Device device, Pool pool, ProgressReporter progressReporter) {
        InstrumentationInfo instrumentationInfo = configuration.getInstrumentationInfo();

        TestRunParameters testRunParameters = testRunParameters()
                .withDeviceInterface(device.getDeviceInterface())
                .withTest(testClass)
                .withTestPackage(instrumentationInfo.getInstrumentationPackage())
                .withTestRunner(instrumentationInfo.getTestRunnerClass())
                .withTestSize(runtimeConfiguration.getTestSize())
                .withTestOutputTimeout(configuration.getTestOutputTimeout())
                .build();

        List<ITestRunListener> testRunListeners = testRunListenersFactory.createTestListeners(
                testClass,
                device,
                pool,
                progressReporter);

        return new TestRun(
                pool.getName(),
                testRunParameters,
                testRunListeners);
    }
}
