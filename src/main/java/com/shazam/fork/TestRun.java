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
package com.shazam.fork;

import com.android.ddmlib.testrunner.*;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestRunParameters;
import com.shazam.fork.runtime.TestRunActivityWatchdog;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.String.format;

class TestRun {
    private final Configuration configuration;
    private final RuntimeConfiguration runtimeConfiguration;
    private final String poolName;
	private final TestRunParameters testRunParameters;
	private final ITestRunListener[] testRunListeners;

    public TestRun(Configuration configuration, RuntimeConfiguration runtimeConfiguration, String poolName,
                   TestRunParameters testRunParameters, ITestRunListener... testRunListeners) {
        this.configuration = configuration;
        this.runtimeConfiguration = runtimeConfiguration;
        this.poolName = poolName;
		this.testRunParameters = testRunParameters;
		this.testRunListeners = testRunListeners;
	}

	public void execute() {
		RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				testRunParameters.getTestPackage(),
				testRunParameters.getTestRunner(),
				testRunParameters.getDeviceInterface());
		TestClass test = testRunParameters.getTest();
		String testClassName = test.getClassName();
		TestRunActivityWatchdog watchdog = new TestRunActivityWatchdog(configuration, runner, test, poolName,
                testRunListeners, testRunParameters.getDeviceInterface().getSerialNumber());
		ArrayList<ITestRunListener> mutableListeners = new ArrayList<>(Arrays.asList(testRunListeners));
		mutableListeners.add(0, watchdog);
		ITestRunListener[] dogInfestedListeners = mutableListeners.toArray(new ITestRunListener[mutableListeners.size()]);
		try {
            IRemoteAndroidTestRunner.TestSize testSize = runtimeConfiguration.getTestSize();
            if (testSize != null) {
                runner.setTestSize(testSize);
            }
			runner.setRunName(poolName);
			runner.setClassName(testClassName);
			runner.run(dogInfestedListeners);
		} catch (Exception e) {
			throw new RuntimeException(format("Error while running test class %s", test), e);
		} finally {
			watchdog.cancel();
			watchdog.flagOutstandingAsErrors(format("Terminated test class early: %s suffered a catastrophy", test.getClassName()));
		}
	}

}
