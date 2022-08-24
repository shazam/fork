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

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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

    private final IDevice deviceInterface;
    private final ScreenRecorderStopper screenRecorderStopper;
    private final ExecutorService recorderExecutor;
    private final ExecutorService fileExecutor;

    private State state = State.Stopped;
    private Future<?> recorderTask;

    public ScreenRecorderImpl(IDevice deviceInterface) {
        this.deviceInterface = deviceInterface;
        this.screenRecorderStopper = new ScreenRecorderStopper(deviceInterface);
        this.recorderExecutor = namedExecutor(/* numberOfThreads = */ 1, "RecorderExecutor-%d");
        this.fileExecutor = namedExecutor(/* numberOfThreads = */ 1, "RecorderFileExecutor-%d");
    }

    @Override
    public void startScreenRecording(TestIdentifier test) {
        if (state != State.Stopped) {
            logger.warn("ScreenRecorder is {} -> stopping screen recording", state);
            stopScreenRecordingInternal();
        }

        state = State.Recording;
        recorderTask = recorderExecutor.submit(() -> {
            try {
                String remoteFilePath = remoteVideoForTest(test);
                logger.debug("Started recording video {}", remoteFilePath);
                startRecordingTestVideo(remoteFilePath);
            } catch (TimeoutException e) {
                logger.debug("Screen recording was either interrupted or timed out", e);
            } catch (Exception e) {
                logger.error("Something went wrong while screen recording", e);
            }
        });
    }

    @Override
    public void stopScreenRecording(TestIdentifier test) {
        if (state != State.Recording) {
            logger.warn("ScreenRecorder is {} when we tried to stop recording", state);
        }
        stopScreenRecordingInternal();
    }

    private void stopScreenRecordingInternal() {
        logger.debug("Stopped screen recording");
        if (recorderTask != null) {
            recorderTask.cancel(true);
        }
        screenRecorderStopper.stopScreenRecord();
        state = State.Stopped;
    }

    @Override
    public void saveScreenRecording(TestIdentifier test, File output) {
        fileExecutor.submit(() -> {
            try {
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
        fileExecutor.submit(() -> {
            try {
                String remoteFilePath = remoteVideoForTest(test);
                logger.debug("Remove screen recording {}", remoteFilePath);
                removeTestVideo(remoteFilePath);
            } catch (Exception e) {
                logger.error("Failed to remove a video file", e);
            }
        });
    }

    private void startRecordingTestVideo(String remoteFilePath) throws TimeoutException,
            AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        NullOutputReceiver outputReceiver = new NullOutputReceiver();
        logger.trace("Started recording video at: {}", remoteFilePath);
        long startNanos = nanoTime();
        deviceInterface.startScreenRecorder(remoteFilePath, RECORDER_OPTIONS, outputReceiver);
        logger.trace("Recording finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
    }

    private void pullTestVideo(String remoteFilePath, File output) throws IOException,
            AdbCommandRejectedException, TimeoutException, SyncException {
        logger.trace("Started pulling file {} to {}", remoteFilePath, output);
        long startNanos = nanoTime();
        deviceInterface.pullFile(remoteFilePath, output.toString());
        logger.trace("Pulling finished in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
    }

    private void removeTestVideo(String remoteFilePath) {
        logger.trace("Started removing file {}", remoteFilePath);
        long startNanos = nanoTime();
        removeRemotePath(deviceInterface, remoteFilePath);
        logger.trace("Removed file in {}ms {}", millisSinceNanoTime(startNanos), remoteFilePath);
    }

    private enum State {
        Recording,
        Stopped
    }
}
