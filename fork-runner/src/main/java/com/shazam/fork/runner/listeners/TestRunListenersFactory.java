/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.google.gson.Gson;
import com.shazam.fork.Configuration;
import com.shazam.fork.device.FileManagerBasedDeviceTestFilesCleaner;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.ProgressReporter;
import com.shazam.fork.runner.ReporterBasedFailedTestScheduler;
import com.shazam.fork.system.io.FileManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Queue;

import static com.shazam.fork.model.Diagnostics.VIDEO;
import static java.util.Arrays.asList;

public class TestRunListenersFactory {

    private final Configuration configuration;
    private final FileManager fileManager;
    private final Gson gson;

    public TestRunListenersFactory(Configuration configuration,
                                   FileManager fileManager,
                                   Gson gson) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        this.gson = gson;
    }

    public List<ITestRunListener> createTestListeners(@Nonnull TestCaseEvent testCase,
                                                      Device device,
                                                      Pool pool,
                                                      ProgressReporter progressReporter,
                                                      Queue<TestCaseEvent> testCaseEventQueue) {
        return asList(
                new ProgressTestRunListener(pool, progressReporter),
                getForkXmlTestRunListener(fileManager, configuration.getOutput(), pool, device, testCase, progressReporter),
                new ConsoleLoggingTestRunListener(configuration.getTestPackage(), device.getSerial(),
                        device.getModelName(), progressReporter),
                new LogCatTestRunListener(gson, fileManager, pool, device),
                new SlowWarningTestRunListener(),
                getScreenTraceTestRunListener(fileManager, pool, device),
                buildRetryListener(device, pool, progressReporter, testCaseEventQueue, testCase),
                getCoverageTestRunListener(configuration, device, fileManager, pool, testCase));
    }

    private RetryListener buildRetryListener(Device device,
                                             Pool pool,
                                             ProgressReporter progressReporter,
                                             Queue<TestCaseEvent> testCaseEventQueue,
                                             TestCaseEvent testCase) {
        ReporterBasedFailedTestScheduler testScheduler =
                new ReporterBasedFailedTestScheduler(progressReporter, pool, testCaseEventQueue);
        FileManagerBasedDeviceTestFilesCleaner deviceTestFilesCleaner = new FileManagerBasedDeviceTestFilesCleaner(fileManager, pool, device);
        return new RetryListener(pool, device, testScheduler, deviceTestFilesCleaner, testCase);
    }

    private ForkXmlTestRunListener getForkXmlTestRunListener(FileManager fileManager,
                                                             File output,
                                                             Pool pool,
                                                             Device device,
                                                             @Nonnull TestCaseEvent testCase,
                                                             ProgressReporter progressReporter) {
        ForkXmlTestRunListener xmlTestRunListener = new ForkXmlTestRunListener(fileManager, pool, device, testCase, progressReporter);
        xmlTestRunListener.setReportDir(output);
        return xmlTestRunListener;
    }

    private ITestRunListener getCoverageTestRunListener(Configuration configuration,
                                                        Device device,
                                                        FileManager fileManager,
                                                        Pool pool,
                                                        TestCaseEvent testCase) {
        if (configuration.isCoverageEnabled()) {
            return new CoverageListener(device, fileManager, pool, testCase);
        }
        return new NoOpITestRunListener();
    }

    private ITestRunListener getScreenTraceTestRunListener(FileManager fileManager, Pool pool, Device device) {
        if (VIDEO.equals(device.getSupportedDiagnostics())) {
            return new ScreenRecorderTestRunListener(fileManager, pool, device);
        }

        return new NoOpITestRunListener();
    }
}
