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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;

import java.io.File;
import java.util.Map;

import static com.shazam.fork.system.io.FileType.SCREENRECORD;

class ScreenRecorderTestRunListener implements ITestRunListener {
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;
    private final IDevice deviceInterface;

    private boolean hasFailed;
    private ScreenRecorderStopper screenRecorderStopper;

    public ScreenRecorderTestRunListener(FileManager fileManager, Pool pool, Device device) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
        deviceInterface = device.getDeviceInterface();
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
    }

    @Override
    public void testStarted(TestIdentifier test) {
        hasFailed = false;
        File localVideoFile = fileManager.createFile(SCREENRECORD, pool, device, test);
        screenRecorderStopper = new ScreenRecorderStopper(deviceInterface);
        ScreenRecorder screenRecorder = new ScreenRecorder(test, screenRecorderStopper, localVideoFile, deviceInterface);
        new Thread(screenRecorder, "ScreenRecorder").start();
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        hasFailed = true;
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
        screenRecorderStopper.stopScreenRecord(hasFailed);
    }

    @Override
    public void testIgnored(TestIdentifier test) {
        screenRecorderStopper.stopScreenRecord(hasFailed);
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        screenRecorderStopper.stopScreenRecord(hasFailed);
    }

    @Override
    public void testRunFailed(String errorMessage) {
    }

    @Override
    public void testRunStopped(long elapsedTime) {
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
    }
}
