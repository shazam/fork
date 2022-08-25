/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.device.ScreenRecorder;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;

import java.io.File;
import java.util.Map;

import static com.shazam.fork.system.io.FileType.SCREENRECORD;

class ScreenRecorderTestRunListener extends NoOpITestRunListener {
    private final FileManager fileManager;
    private final ScreenRecorder screenRecorder;
    private final Pool pool;
    private final Device device;
    private TestIdentifier currentTest;

    public ScreenRecorderTestRunListener(FileManager fileManager, ScreenRecorder screenRecorder, Pool pool, Device device) {
        this.fileManager = fileManager;
        this.screenRecorder = screenRecorder;
        this.pool = pool;
        this.device = device;
    }

    @Override
    public void testStarted(TestIdentifier test) {
        currentTest = test;
        screenRecorder.startScreenRecording(test);
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        saveScreenRecording(test);
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        screenRecorder.stopScreenRecording(test);
    }

    @Override
    public void testRunFailed(String errorMessage) {
        // highly likely that something crashed and we never received testEnded
        if (currentTest != null) {
            saveScreenRecording(currentTest);
        }
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        if (currentTest != null) {
            screenRecorder.removeScreenRecording(currentTest);
        }
    }

    private void saveScreenRecording(TestIdentifier test) {
        File outputScreenRecording = fileManager.createFile(SCREENRECORD, pool, device, test);
        screenRecorder.saveScreenRecording(test, outputScreenRecording);
    }
}
