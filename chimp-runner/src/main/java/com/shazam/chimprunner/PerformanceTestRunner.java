/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

import com.android.ddmlib.*;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.adb.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;

public class PerformanceTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestRunner.class);

    private final Installer installer;
    private final String instrumentationPackage;
    private final String testRunner;

    public PerformanceTestRunner(Installer installer,
                                 String instrumentationPackage,
                                 String testRunner) {
        this.installer = installer;
        this.instrumentationPackage = instrumentationPackage;
        this.testRunner = testRunner;
    }

    public void run(Device device, Collection<TestCaseEvent> testCaseEvents) throws TestFailureException {
        installer.prepareInstallation(device.getDeviceInterface());
        LinkedList<TestCaseEvent> testQueue = new LinkedList<>(testCaseEvents);
        TestCaseEvent testCaseEvent;
        while ((testCaseEvent = testQueue.poll()) != null) {
            runTest(device, testCaseEvent);
        }
    }

    private void runTest(Device device, TestCaseEvent testCaseEvent) throws TestFailureException {
        RemoteAndroidTestRunner androidTestRunner = new RemoteAndroidTestRunner(instrumentationPackage, testRunner,
                device.getDeviceInterface());
        String testClassName = testCaseEvent.getTestClass();
        String testMethodName = testCaseEvent.getTestMethod();
        androidTestRunner.setMethodName(testClassName, testMethodName);
        androidTestRunner.setMaxtimeToOutputResponse(Defaults.ADB_MAX_TIME_TO_OUTPUT_RESPONSE);
        try {
            PerformanceTestListener performanceTestListener = new LoggingPerformanceTestListener(testClassName, testMethodName);
            performanceTestListener.startOverall();
            int iterations = Defaults.ITERATIONS;
            for (int i = 0; i < iterations; i++) {
                performanceTestListener.startIteration();
                ResultObservingTestRunListener resultObservingTestRunListener = new ResultObservingTestRunListener();
                androidTestRunner.run(resultObservingTestRunListener);
                if (resultObservingTestRunListener.hasFailed()) {
                    throw new TestFailureException(resultObservingTestRunListener.getTestFailure());
                }
                performanceTestListener.endIteration();
            }
            performanceTestListener.endOverall();
        } catch (ShellCommandUnresponsiveException | TimeoutException e) {
            logger.warn("Test: " + testClassName + " got stuck. You can increase the timeout in settings if it's too strict");
        } catch (AdbCommandRejectedException | IOException e) {
            throw new RuntimeException(format("Error while running test %s %s", testClassName, testMethodName), e);
        }
    }

}
