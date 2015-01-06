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
package com.shazam.fork.runtime;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.system.CancellableShellOutputReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static com.shazam.fork.io.RemoteDirectory.remoteForkDirectory;
import static com.shazam.fork.io.RemoteFileManager.removeRemotePath;
import static com.shazam.fork.runtime.ScreenRecorderFilenameFactory.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ScreenRecorderTestRunListener implements ITestRunListener {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorderTestRunListener.class);
    private final CancellableShellOutputReceiver cancellableReceiver = new CancellableShellOutputReceiver();
    private static final int DURATION = 15;
    private static final int BIT_RATE_MBPS = 2;
    private static final ScreenRecorderOptions RECORDER_OPTIONS = new ScreenRecorderOptions.Builder()
            .setTimeLimit(DURATION, SECONDS)
            .setBitRate(BIT_RATE_MBPS)
            .build();

    private final IDevice deviceInterface;
    private final File output;
    private final Device device;
    private final String remoteDirectory;

    private boolean hasFailed;

    public ScreenRecorderTestRunListener(File output, Device device) {
        this.output = output;
        this.device = device;
        deviceInterface = device.getDeviceInterface();
        remoteDirectory = remoteForkDirectory();
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
    }

    @Override
    public void testStarted(final TestIdentifier test) {
        hasFailed = false;
        try {
            String remoteFilePath = remoteVideoForTest(remoteDirectory, test);
            deviceInterface.startScreenRecorder(remoteFilePath, RECORDER_OPTIONS, cancellableReceiver);
        } catch (Exception e) {
            logger.error("Could not start screen recorder", e);
        }
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        hasFailed = true;
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
        cancellableReceiver.cancel();
    }

    @Override
    public void testIgnored(TestIdentifier test) {
        cancellableReceiver.cancel();
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        String remoteVideoPathName = remoteVideoForTest(remoteDirectory, test);
        cancellableReceiver.cancel();

        try {
            if (hasFailed) {
                String localVideoPathName = localVideoPathName(output, device, test);
                deviceInterface.pullFile(remoteVideoPathName, localVideoPathName);
            }
        } catch (Exception e) {
            logger.warn("Failed to pull video file: " + e.getMessage());
        }
    }

    @Override
    public void testRunFailed(String errorMessage) {
    }

    @Override
    public void testRunStopped(long elapsedTime) {
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        removeRemotePath(deviceInterface, videosIn(remoteDirectory));
    }
}
