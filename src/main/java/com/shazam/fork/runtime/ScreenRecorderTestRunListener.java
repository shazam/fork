/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runtime;

import com.android.ddmlib.*;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.io.FileManager;
import com.shazam.fork.io.RemoteFileManager;
import com.shazam.fork.model.Device;
import com.shazam.fork.system.CancellableShellOutputReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.shazam.fork.Utils.millisSinceNanoTime;
import static com.shazam.fork.io.FileType.SCREENRECORD;
import static com.shazam.fork.io.RemoteFileManager.remoteVideoForTest;
import static com.shazam.fork.io.RemoteFileManager.removeRemotePath;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * TODO: Currently does not kill screenrecord process, but records the first 20 seconds after a test starting.
 * Need to do be able to find the PID and stop it. Only one test runs on a device, so only one screenrecord.
 *
 * http://stackoverflow.com/questions/25185746/stop-adb-screenrecord-from-java
 */
public class ScreenRecorderTestRunListener implements ITestRunListener {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorderTestRunListener.class);
    private static final int DURATION = 20;
    private static final int BIT_RATE_MBPS = 1;
    private static final ScreenRecorderOptions RECORDER_OPTIONS = new ScreenRecorderOptions.Builder()
            .setTimeLimit(DURATION, SECONDS)
            .setBitRate(BIT_RATE_MBPS)
            .build();

    private final FileManager fileManager;
    private final String pool;
    private final Device device;
    private final IDevice deviceInterface;
    private final String remoteDirectory;

    private boolean hasFailed;
    private CancellableShellOutputReceiver cancellableReceiver;

    public ScreenRecorderTestRunListener(FileManager fileManager, String pool, Device device) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
        deviceInterface = device.getDeviceInterface();
        remoteDirectory = RemoteFileManager.FORK_DIRECTORY;
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
    }

    @Override
    public void testStarted(TestIdentifier test) {
        hasFailed = false;
        cancellableReceiver = new CancellableShellOutputReceiver();
        String remoteFilePath = remoteVideoForTest(remoteDirectory, test);

        File localVideoFile = fileManager.createFile(SCREENRECORD, pool, device.getSerial(), test);
        new Thread(new ScreenRecorder(remoteFilePath, localVideoFile, deviceInterface, cancellableReceiver))
                .start();
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
        if (!hasFailed) {
            cancellableReceiver.cancel();
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
    }

    private static class ScreenRecorder implements Runnable {
        private final String remoteFilePath;
        private final File localVideoFile;
        private final IDevice deviceInterface;
        private final IShellOutputReceiver outputReceiver;

        public ScreenRecorder(String remoteFilePath, File localVideoFile, IDevice deviceInterface,
                              IShellOutputReceiver outputReceiver) {
            this.remoteFilePath = remoteFilePath;
            this.localVideoFile = localVideoFile;
            this.deviceInterface = deviceInterface;
            this.outputReceiver = outputReceiver;
        }

        @Override
        public void run() {
            try {
                startRecordingTestVideo();
                if (!outputReceiver.isCancelled()) {
                    pullTestVideo();
                }
                removeTestVideo();
            } catch (Exception e) {
                logger.error("Something went wrong while screen recording", e);
            }
        }

        private void startRecordingTestVideo() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
            logger.debug("Started recording video at: {}", remoteFilePath);
            long startNanos = nanoTime();
            deviceInterface.startScreenRecorder(remoteFilePath, RECORDER_OPTIONS, outputReceiver);
            logger.debug("Recording finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
        }

        private void pullTestVideo() throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {
            logger.debug("Started pulling file {} to {}", remoteFilePath, localVideoFile);
            long startNanos = nanoTime();
            deviceInterface.pullFile(remoteFilePath, localVideoFile.toString());
            logger.debug("Pulling finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
        }

        private void removeTestVideo() {
            logger.debug("Started removing file {}", remoteFilePath);
            long startNanos = nanoTime();
            removeRemotePath(deviceInterface, remoteFilePath);
            logger.debug("Removed file in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
        }
    }
}
