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
package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.*;
import com.shazam.fork.Configuration;
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.*;
import com.shazam.fork.listeners.TestRunActivityWatchdog;

import java.util.*;

import static java.lang.String.format;

class TestRun {
    private final Configuration configuration;
    private final RuntimeConfiguration runtimeConfiguration;
    private final String poolName;
	private final TestRunParameters testRunParameters;
	private final List<ITestRunListener> testRunListeners;

    public TestRun(Configuration configuration,
                   RuntimeConfiguration runtimeConfiguration,
                   String poolName,
                   TestRunParameters testRunParameters,
                   Collection<ITestRunListener> testRunListeners) {
        this.configuration = configuration;
        this.runtimeConfiguration = runtimeConfiguration;
        this.poolName = poolName;
		this.testRunParameters = testRunParameters;
		this.testRunListeners = new ArrayList<>(testRunListeners);
	}

	public void execute() {
		RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				testRunParameters.getTestPackage(),
				testRunParameters.getTestRunner(),
				testRunParameters.getDeviceInterface());

		TestClass test = testRunParameters.getTest();
        TestRunActivityWatchdog watchdog = watchdog(runner, test);
        String testClassName = test.getName();
		try {
            IRemoteAndroidTestRunner.TestSize testSize = runtimeConfiguration.getTestSize();
            if (testSize != null) {
                runner.setTestSize(testSize);
            }
			runner.setRunName(poolName);
			runner.setClassName(testClassName);
			runner.run(testRunListenersAdding(watchdog));
		} catch (Exception e) {
			throw new RuntimeException(format("Error while running test class %s", test), e);
		} finally {
			watchdog.cancel();
			watchdog.flagOutstandingAsErrors(format("Terminated test class early: %s suffered a catastrophy", test.getName()));
		}
	}

    private TestRunActivityWatchdog watchdog(RemoteAndroidTestRunner runner, TestClass test) {
        return new TestRunActivityWatchdog(configuration, runner, test, poolName,
                testRunListeners, testRunParameters.getDeviceInterface().getSerialNumber());
    }

    private List<ITestRunListener> testRunListenersAdding(ITestRunListener testRunListener) {
        testRunListeners.add(0, testRunListener);
        return testRunListeners;
    }
}
