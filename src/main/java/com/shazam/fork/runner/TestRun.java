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
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestRunParameters;

import java.util.Collection;

import static java.lang.String.format;

class TestRun {
    private final RuntimeConfiguration runtimeConfiguration;
    private final String poolName;
	private final TestRunParameters testRunParameters;
	private final Collection<ITestRunListener> testRunListeners;

    public TestRun(RuntimeConfiguration runtimeConfiguration,
                   String poolName,
                   TestRunParameters testRunParameters,
                   Collection<ITestRunListener> testRunListeners) {
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
        String testClassName = test.getName();
		try {
            IRemoteAndroidTestRunner.TestSize testSize = runtimeConfiguration.getTestSize();
            if (testSize != null) {
                runner.setTestSize(testSize);
            }
			runner.setRunName(poolName);
			runner.setClassName(testClassName);
			runner.run(testRunListeners);
		} catch (Exception e) {
			throw new RuntimeException(format("Error while running test class %s", test), e);
		}
	}
}
