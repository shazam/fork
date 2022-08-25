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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Strings;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.PermissionGrantingManager;
import com.shazam.fork.system.io.RemoteFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

class TestRun {
	private static final Logger logger = LoggerFactory.getLogger(TestRun.class);
    private final String poolName;
	private final TestRunParameters testRunParameters;
	private final List<ITestRunListener> testRunListeners;
	private final PermissionGrantingManager permissionGrantingManager;

	public TestRun(String poolName,
				   TestRunParameters testRunParameters,
				   List<ITestRunListener> testRunListeners,
				   PermissionGrantingManager permissionGrantingManager) {
        this.poolName = poolName;
		this.testRunParameters = testRunParameters;
		this.testRunListeners = testRunListeners;
		this.permissionGrantingManager = permissionGrantingManager;
	}

	public void execute() {
		String applicationPackage = testRunParameters.getApplicationPackage();
		IDevice device = testRunParameters.getDeviceInterface();

		RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				testRunParameters.getTestPackage(),
				testRunParameters.getTestRunner(),
				device);

		TestCaseEvent test = testRunParameters.getTest();
		String testClassName = test.getTestClass();
		String testMethodName = test.getTestMethod();
		IRemoteAndroidTestRunner.TestSize testSize = testRunParameters.getTestSize();
		if (testSize != null) {
			runner.setTestSize(testSize);
		}
		runner.setRunName(poolName);
		runner.setMethodName(testClassName, testMethodName);
		runner.setMaxtimeToOutputResponse(testRunParameters.getTestOutputTimeout());

        if (testRunParameters.isCoverageEnabled()) {
            runner.setCoverage(true);
			String coverageFileName =
					RemoteFileManager.getCoverageFileName(device, new TestIdentifier(testClassName, testMethodName));
			runner.addInstrumentationArg("coverageFile", coverageFileName);
        }
		String excludedAnnotation = testRunParameters.getExcludedAnnotation();
		if (!Strings.isNullOrEmpty(excludedAnnotation)) {
			logger.info("Tests annotated with {} will be excluded", excludedAnnotation);
			runner.addInstrumentationArg("notAnnotation", excludedAnnotation);
		} else {
			logger.info("No excluding any test based on annotations");
		}

		List<String> permissionsToRevoke = testRunParameters.getTest().getPermissionsToRevoke();

		permissionGrantingManager.revokePermissions(applicationPackage, device, permissionsToRevoke);

		try {
			runner.run(testRunListeners);
		} catch (ShellCommandUnresponsiveException | TimeoutException e) {
			logger.warn("Test: " + testClassName + " got stuck. You can increase the timeout in settings if it's too strict");
		} catch (AdbCommandRejectedException | IOException e) {
			throw new RuntimeException(format("Error while running test %s %s", test.getTestClass(), test.getTestMethod()), e);
		} finally {
			permissionGrantingManager.restorePermissions(applicationPackage, device, permissionsToRevoke);
		}

    }
}
