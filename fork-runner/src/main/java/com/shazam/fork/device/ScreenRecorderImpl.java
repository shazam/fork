/*
 * Copyright 2022 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shazam.fork.device;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.shazam.fork.Utils.namedExecutor;
import static com.shazam.fork.system.io.RemoteFileManager.remoteVideoForTest;
import static com.shazam.fork.system.io.RemoteFileManager.removeRemotePath;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ScreenRecorderImpl implements ScreenRecorder {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorderImpl.class);
    private static final int DURATION = 60;
    private static final int BIT_RATE_MBPS = 1;
    private static final ScreenRecorderOptions RECORDER_OPTIONS = new ScreenRecorderOptions.Builder()
            .setTimeLimit(DURATION, SECONDS)
            .setBitRate(BIT_RATE_MBPS)
            .build();

    private final Device device;
    private final ScreenRecorderStopper screenRecorderStopper;
    private final ExecutorService recorderExecutor =
            namedExecutor(/* numberOfThreads = */ 1, "RecorderExecutor-%d");
    private final ExecutorService fileExecutor =
            namedExecutor(/* numberOfThreads = */ 1, "RecorderFileExecutor-%d");

    private State state = State.Stopped;
    private final Map<TestIdentifier, RecorderTask> recorderTasksProjection =
            new ConcurrentHashMap<>();

    public ScreenRecorderImpl(Device device) {
        this.device = device;
        this.screenRecorderStopper = new ScreenRecorderStopper(device);
    }

    @Override
    public void startScreenRecording(TestIdentifier test) {
        if (state != State.Stopped) {
            logger.warn("ScreenRecorder is {} -> stopping screen recording", state);
            stopActiveScreenRecording();
        }

        state = State.Recording;
        RecorderTask recorderTask = new RecorderTask(test, device);
        recorderTasksProjection.put(test, recorderTask);
        recorderExecutor.submit(recorderTask);
    }

    @Override
    public void stopScreenRecording(TestIdentifier test) {
        if (state != State.Recording) {
            logger.warn("ScreenRecorder is {} when we tried to stop recording", state);
        }
        logger.debug("Stopped screen recording");

        stopActiveScreenRecording();
    }

    private void stopActiveScreenRecording() {
        screenRecorderStopper.stopScreenRecord();
        state = State.Stopped;
    }

    @Override
    public void saveScreenRecording(TestIdentifier test, File output) {
        RecorderTask recorderTask = recorderTasksProjection.get(test);
        if (recorderTask == null) {
            logger.warn("Recording for {} was not found", test);
            return;
        }

        fileExecutor.submit(() -> {
            try {
                recorderTask.awaitCompletion();

                String remoteFilePath = remoteVideoForTest(test);
                logger.debug("Save screen recording {} to {}", remoteFilePath, output);
                pullTestVideo(remoteFilePath, output);
            } catch (Exception e) {
                logger.error("Failed to pull a video file", e);
            }
        });
    }

    @Override
    public void removeScreenRecording(TestIdentifier test) {
        RecorderTask recorderTask = recorderTasksProjection.get(test);
        if (recorderTask == null) {
            logger.warn("Recording for {} was not found", test);
            return;
        }

        fileExecutor.submit(() -> {
            try {
                recorderTask.awaitCompletion();

                String remoteFilePath = remoteVideoForTest(test);
                logger.debug("Remove screen recording {}", remoteFilePath);
                removeTestVideo(remoteFilePath);

                recorderTasksProjection.remove(test);
            } catch (Exception e) {
                logger.error("Failed to remove a video file", e);
            }
        });
    }

    private void pullTestVideo(String remoteFilePath, File output) throws IOException,
            AdbCommandRejectedException, TimeoutException, SyncException {
        logger.trace("Started pulling file {} to {}", remoteFilePath, output);
        long startNanos = nanoTime();
        device.getDeviceInterface().pullFile(remoteFilePath, output.toString());
        logger.trace("Pulling finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
    }

    private void removeTestVideo(String remoteFilePath) {
        logger.trace("Started removing file {}", remoteFilePath);
        long startNanos = nanoTime();
        removeRemotePath(device.getDeviceInterface(), remoteFilePath);
        logger.trace("Removed file in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
    }

    private static class RecorderTask implements Runnable {
        private final TestIdentifier test;
        private final IDevice deviceInterface;
        private final CountDownLatch latch = new CountDownLatch(1);

        public RecorderTask(TestIdentifier test, Device device) {
            this.test = test;
            this.deviceInterface = device.getDeviceInterface();
        }

        public void awaitCompletion() throws InterruptedException {
            latch.await();
        }

        @Override
        public void run() {
            try {
                String remoteFilePath = remoteVideoForTest(test);
                logger.debug("Started recording video {}", remoteFilePath);

                startRecordingTestVideo(remoteFilePath);

                latch.countDown();

                logger.debug("Video recording finished {}", remoteFilePath);
            } catch (TimeoutException e) {
                logger.debug("Screen recording was either interrupted or timed out", e);
            } catch (Exception e) {
                logger.error("Something went wrong while screen recording", e);
            }
        }

        private void startRecordingTestVideo(String remoteFilePath) throws TimeoutException,
                AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
            logger.trace("Started recording video at: {}", remoteFilePath);
            long startNanos = nanoTime();
            deviceInterface.startScreenRecorder(remoteFilePath, RECORDER_OPTIONS, new NullOutputReceiver());
            logger.trace("Recording finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
        }
    }

    private enum State {
        Recording,
        Stopped
    }
}
