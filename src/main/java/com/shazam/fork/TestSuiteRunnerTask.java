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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.google.gson.Gson;
import com.shazam.fork.io.FileManager;
import com.shazam.fork.model.*;
import com.shazam.fork.runtime.*;
import com.shazam.fork.system.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import static com.shazam.fork.io.RemoteFileManager.createRemoteDirectory;
import static com.shazam.fork.io.RemoteFileManager.removeRemoteDirectory;
import static com.shazam.fork.model.Diagnostics.VIDEO;
import static com.shazam.fork.model.TestRunParameters.Builder.testRunParameters;

class TestSuiteRunnerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteRunnerTask.class);

    private final Configuration configuration;
    private final RuntimeConfiguration runtimeConfiguration;
    private final Gson gson;
    private final Installer installer;
    private final String poolName;
    private final Device device;
    private final Queue<TestClass> testClassQueue;
    private final CountDownLatch deviceCountDownLatch;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final FileManager fileManager;

    public TestSuiteRunnerTask(Configuration configuration,
                               RuntimeConfiguration runtimeConfiguration,
                               Gson gson,
                               Installer installer,
                               FileManager fileManager,
                               String poolName,
                               Device device,
                               Queue<TestClass> testClassQueue,
                               CountDownLatch deviceCountDownLatch,
                               SwimlaneConsoleLogger swimlaneConsoleLogger) {
        this.configuration = configuration;
        this.runtimeConfiguration = runtimeConfiguration;
        this.gson = gson;
        this.fileManager = fileManager;
        this.installer = installer;
		this.poolName = poolName;
		this.device = device;
        this.testClassQueue = testClassQueue;
        this.deviceCountDownLatch = deviceCountDownLatch;
		this.swimlaneConsoleLogger = swimlaneConsoleLogger;
	}

	@Override
	public void run() {
        String serial = device.getSerial();
        IDevice deviceInterface = device.getDeviceInterface();
        try {
            swimlaneConsoleLogger.setCount(serial, poolName, testClassQueue.size());
            installer.prepareInstallation(deviceInterface);
            // For when previous run crashed/disconnected and left files behind
            removeRemoteDirectory(deviceInterface);
            createRemoteDirectory(deviceInterface);

            TestClass testClass;
            while ((testClass = testClassQueue.poll()) != null) {
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
        InstrumentationInfo instrumentationInfo = configuration.getInstrumentationInfo();

        TestRunParameters testRunParameters = testRunParameters()
				.withDeviceInterface(device.getDeviceInterface())
				.withTest(testClass)
				.withTestPackage(instrumentationInfo.getInstrumentationPackage())
				.withTestRunner(instrumentationInfo.getTestRunnerClass())
				.build();
        File output = configuration.getOutput();
        ITestRunListener xmlTestRunListener = getForkXmlTestRunListener(fileManager, output, poolName,
                device.getSerial(), testClass);
        ITestRunListener logTestRunListener = new LoggingTestRunListener(device.getSerial(), swimlaneConsoleLogger);
        ITestRunListener logCatTestRunListener = new LogCatTestRunListener(gson, fileManager, poolName, device);
        ITestRunListener screenTraceTestRunListener = getScreenTraceTestRunListener(fileManager, poolName, device);
        ITestRunListener slowTestWarningTestRunListener = new SlowWarningTestRunListener();

        TestRun testRun = new TestRun(
                configuration,
                runtimeConfiguration,
                poolName,
				testRunParameters,
				xmlTestRunListener,
                logTestRunListener,
				logCatTestRunListener,
                slowTestWarningTestRunListener,
                screenTraceTestRunListener);
		testRun.execute();
	}

    public static ForkXmlTestRunListener getForkXmlTestRunListener(FileManager fileManager,
                                                                   File output,
                                                                   String poolName,
                                                                   String serial,
                                                                   TestClass testClass) {
        ForkXmlTestRunListener xmlTestRunListener = new ForkXmlTestRunListener(fileManager, poolName, serial, testClass);
        xmlTestRunListener.setReportDir(output);
        return xmlTestRunListener;
    }

    private static ITestRunListener getScreenTraceTestRunListener(FileManager fileManager, String pool, Device device) {
        if (VIDEO.equals(device.getSupportedDiagnostics())) {
            return new ScreenRecorderTestRunListener(fileManager, pool, device);
        }
        return new ScreenCaptureTestRunListener(fileManager, pool, device);
    }
}
