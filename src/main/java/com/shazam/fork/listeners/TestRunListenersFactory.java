/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.listeners;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.google.gson.Gson;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.system.io.FileManager;

import java.io.File;
import java.util.List;

import static com.shazam.fork.model.Diagnostics.SCREENSHOTS;
import static com.shazam.fork.model.Diagnostics.VIDEO;
import static java.util.Arrays.asList;

public class TestRunListenersFactory {

    private final Configuration configuration;
    private final FileManager fileManager;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final Gson gson;

    public TestRunListenersFactory(Configuration configuration, FileManager fileManager,
                                   SwimlaneConsoleLogger swimlaneConsoleLogger, Gson gson) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        this.swimlaneConsoleLogger = swimlaneConsoleLogger;
        this.gson = gson;
    }

    public List<ITestRunListener> createTestListeners(TestClass testClass, Device device, String poolName) {
        return asList(
                getForkXmlTestRunListener(fileManager, configuration.getOutput(), poolName, device.getSerial(), testClass),
                new LoggingTestRunListener(device.getSerial(), swimlaneConsoleLogger),
                new LogCatTestRunListener(gson, fileManager, poolName, device),
                new SlowWarningTestRunListener(),
                getScreenTraceTestRunListener(fileManager, poolName, device));
    }

    public static ForkXmlTestRunListener getForkXmlTestRunListener(FileManager fileManager, File output, String poolName,
                                                                   String serial, TestClass testClass) {
        ForkXmlTestRunListener xmlTestRunListener = new ForkXmlTestRunListener(fileManager, poolName, serial, testClass);
        xmlTestRunListener.setReportDir(output);
        return xmlTestRunListener;
    }

    private ITestRunListener getScreenTraceTestRunListener(FileManager fileManager, String pool, Device device) {
        if (VIDEO.equals(device.getSupportedDiagnostics())) {
            return new ScreenRecorderTestRunListener(fileManager, pool, device);
        }

        if (SCREENSHOTS.equals(device.getSupportedDiagnostics()) && configuration.canFallbackToScreenshots()) {
            return new ScreenCaptureTestRunListener(fileManager, pool, device);
        }

        return new NoOpITestRunListener();
    }
}
