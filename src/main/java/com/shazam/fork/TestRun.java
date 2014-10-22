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

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestRunParameters;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.String.format;

class TestRun {
    private final Configuration configuration;
	private final String poolName;
	private final TestRunParameters testRunParameters;
	private final ITestRunListener[] testRunListeners;

    public TestRun(Configuration configuration, String poolName, TestRunParameters testRunParameters,
                   ITestRunListener... testRunListeners) {
        this.configuration = configuration;
        this.poolName = poolName;
		this.testRunParameters = testRunParameters;
		this.testRunListeners = testRunListeners;
	}

	public void execute() {
		final RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				testRunParameters.getTestPackage(),
				testRunParameters.getTestRunner(),
				testRunParameters.getDeviceInterface());
		final TestClass test = testRunParameters.getTest();
		final String testClassName = test.getClassName();
		TestRunActivityWatchdog watchdog = new TestRunActivityWatchdog(configuration, runner, test, poolName,
                testRunListeners, testRunParameters.getDeviceInterface().getSerialNumber());
		ArrayList<ITestRunListener> mutableListeners = new ArrayList<>(Arrays.asList(testRunListeners));
		mutableListeners.add(0, watchdog);
		ITestRunListener[] dogInfestedListeners = mutableListeners.toArray(new ITestRunListener[mutableListeners.size()]);
		try {
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
