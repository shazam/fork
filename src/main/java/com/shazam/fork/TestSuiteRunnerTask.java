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
import com.google.gson.Gson;
import com.shazam.fork.io.FilenameCreator;
import com.shazam.fork.model.*;
import com.shazam.fork.runtime.*;
import com.shazam.fork.system.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static com.android.ddmlib.IDevice.Feature.SCREEN_RECORD;
import static com.shazam.fork.io.RemoteDirectory.remoteForkDirectory;
import static com.shazam.fork.io.RemoteFileManager.createRemoteDirectory;
import static com.shazam.fork.io.RemoteFileManager.removeRemoteDirectory;
import static com.shazam.fork.model.TestRunParameters.Builder.testRunParameters;

class TestSuiteRunnerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteRunnerTask.class);

    private final Configuration configuration;
    private final Gson gson;
    private final Installer installer;
    private final String poolName;
    private final Device device;
    private final TestClassProvider testClassProvider;
    private final CountDownLatch deviceCountDownLatch;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final FilenameCreator filenameCreator;

    public TestSuiteRunnerTask(Configuration configuration,
                               Gson gson,
                               Installer installer,
                               FilenameCreator filenameCreator,
                               String poolName,
                               Device device,
                               TestClassProvider testClassProvider,
                               CountDownLatch deviceCountDownLatch,
                               SwimlaneConsoleLogger swimlaneConsoleLogger) {
        this.configuration = configuration;
        this.gson = gson;
        this.filenameCreator = filenameCreator;
        this.installer = installer;
		this.poolName = poolName;
		this.device = device;
		this.testClassProvider = testClassProvider;
        this.deviceCountDownLatch = deviceCountDownLatch;
		this.swimlaneConsoleLogger = swimlaneConsoleLogger;
	}

	@Override
	public void run() {
        String serial = device.getSerial();
        IDevice deviceInterface = device.getDeviceInterface();
        String remoteDirectory = remoteForkDirectory();
        try {
            swimlaneConsoleLogger.setCount(serial, poolName, testClassProvider.size());
            installer.prepareInstallation(deviceInterface);
            // For when previous run crashed/disconnected and left files behind
            removeRemoteDirectory(deviceInterface, remoteDirectory);
            createRemoteDirectory(deviceInterface, remoteDirectory);

            TestClass testClass;
            while ((testClass = testClassProvider.getNextTest()) != null) {
				runIndividualTestClass(testClass);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
            logger.info("Device {} from pool {} finished", device.getSerial(), poolName);
			deviceCountDownLatch.countDown();
            removeRemoteDirectory(deviceInterface, remoteDirectory);
		}
	}

    private void runIndividualTestClass(TestClass testClass) {
        InstrumentationInfo instrumentationInfo = configuration.getInstrumentationInfo();

        TestRunParameters testRunParameters = testRunParameters()
				.withDeviceInterface(device.getDeviceInterface())
				.withTest(testClass)
				.withTestPackage(instrumentationInfo.getInstrumentationPackage())
				.withTestRunner(instrumentationInfo.getTestRunnerClass())
				.build();
        File output = configuration.getOutput();
        ITestRunListener xmlTestRunListener = getForkXmlTestRunListener(
                filenameCreator,
                output,
                poolName,
                device.getSerial(),
                testClass);
        ITestRunListener logTestRunListener = new LoggingTestRunListener(device.getSerial(), swimlaneConsoleLogger);
        ITestRunListener logCatTestRunListener = new LogCatTestRunListener(gson, filenameCreator, poolName, device);
        ITestRunListener screenRecorderTestRunListener = getScreenRecorderTestRunListener(output);
        
		TestRun testRun = new TestRun(
                configuration,
                poolName,
				testRunParameters,
				xmlTestRunListener,
                logTestRunListener,
				logCatTestRunListener,
                screenRecorderTestRunListener);
		testRun.execute();
	}

    public static ForkXmlTestRunListener getForkXmlTestRunListener(FilenameCreator filenameCreator,
                                                                   File output,
                                                                   String poolName,
                                                                   String serial,
                                                                   TestClass testClass) {
        ForkXmlTestRunListener xmlTestRunListener = new ForkXmlTestRunListener(filenameCreator, poolName, serial, testClass);
        xmlTestRunListener.setReportDir(output);
        return xmlTestRunListener;
    }

    private ITestRunListener getScreenRecorderTestRunListener(File output) {
        if (device.getDeviceInterface().supportsFeature(SCREEN_RECORD)) {
            return new ScreenRecorderTestRunListener(output, device);
        }
        return new NoOpTestRunListener();
    }
}
