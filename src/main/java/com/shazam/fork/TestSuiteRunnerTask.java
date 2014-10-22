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

import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestRunParameters;
import com.shazam.fork.runtime.*;
import com.shazam.fork.system.Installer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static com.shazam.fork.model.TestRunParameters.Builder.testRunParameters;

class TestSuiteRunnerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteRunnerTask.class);

    private final Configuration configuration;
	private final Installer installer;
	private final String poolName;
	private final Device device;
	private final TestClassProvider testClassProvider;
    private final CountDownLatch deviceCountDownLatch;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;

	public TestSuiteRunnerTask(Configuration configuration, Installer installer, String poolName, Device device,
                               TestClassProvider testClassProvider, CountDownLatch deviceCountDownLatch,
                               SwimlaneConsoleLogger swimlaneConsoleLogger) {
        this.configuration = configuration;
        this.installer = installer;
		this.poolName = poolName;
		this.device = device;
		this.testClassProvider = testClassProvider;
        this.deviceCountDownLatch = deviceCountDownLatch;
		this.swimlaneConsoleLogger = swimlaneConsoleLogger;
	}

	@Override
	public void run() {
		try {
			String serial = device.getSerial();
			swimlaneConsoleLogger.setCount(serial, poolName, testClassProvider.size());
			installer.prepareInstallation(device.getDeviceInterface());
			TestClass testClass;
			while ((testClass = testClassProvider.getNextTest()) != null) {
				runIndividualTestClass(testClass);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
            logger.info("Device {} from pool {} finished", device.getSerial(), poolName);
			deviceCountDownLatch.countDown();
		}
	}

	private void runIndividualTestClass(TestClass testClass) {
		TestRunParameters testRunParameters = testRunParameters()
				.withDeviceInterface(device.getDeviceInterface())
				.withTest(testClass)
				.withTestPackage(configuration.getInstrumentationInfo().getInstrumentationPackage())
				.withTestRunner(configuration.getInstrumentationInfo().getTestRunnerClass())
				.build();
		ForkXmlTestRunListener xmlTestRunListener = getForkXmlTestRunListener(poolName,
                device.getSerial(), testClass.getClassName(), configuration.getOutput());
		LogTestRunListener logTestRunListener = new LogTestRunListener(device.getSerial(),
                swimlaneConsoleLogger);
		LogCatTestRunListener logCatTestRunListener = new LogCatTestRunListener(poolName, device,
                configuration.getOutput());

		TestRun testRun = new TestRun(
                configuration,
                poolName,
				testRunParameters,
				xmlTestRunListener,
                logTestRunListener,
				logCatTestRunListener);
		testRun.execute();
	}

	public static ForkXmlTestRunListener getForkXmlTestRunListener(String poolName, String serial, String className, File output) {
		ForkXmlTestRunListener xmlTestRunListener = new ForkXmlTestRunListener(poolName, serial, className);
		xmlTestRunListener.setReportDir(output);
		return xmlTestRunListener;
	}
}
