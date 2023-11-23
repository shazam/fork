/*
 * Copyright 2019 Apple Inc.
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
package com.shazam.fork.system.io;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

public class RemoteFileManager {

    private static final Logger logger = LoggerFactory.getLogger(RemoteFileManager.class);
    private static final NullOutputReceiver NO_OP_RECEIVER = new NullOutputReceiver();

    private RemoteFileManager() {
    }

    public static void removeRemotePath(IDevice device, String remotePath) {
        executeCommand(device, "rm " + remotePath, "Could not delete remote file(s): " + remotePath);
    }

    public static void createCoverageDirectory(IDevice device) {
        String coverageDirectory = getCoverageDirectory(device);
        executeCommand(device, "mkdir " + coverageDirectory,
                "Could not create remote directory: " + coverageDirectory);
    }

    public static String getCoverageFileName(IDevice device, TestIdentifier testIdentifier) {
        return getCoverageDirectory(device) + "/" + testIdentifier.toString() + ".ec";
    }

    public static void createRemoteDirectory(IDevice device) {
        String forkDirectory = getForkDirectory(device);
        executeCommand(device, "mkdir " + forkDirectory, "Could not create remote directory: " + forkDirectory);
    }

    public static void removeRemoteDirectory(IDevice device) {
        String forkDirectory = getForkDirectory(device);
        executeCommand(device, "rm -r " + forkDirectory, "Could not delete remote directory: " + forkDirectory);
    }

    private static void executeCommand(IDevice device, String command, String errorMessage) {
        try {
            device.executeShellCommand(command, NO_OP_RECEIVER);
        } catch (TimeoutException | AdbCommandRejectedException |
                 ShellCommandUnresponsiveException | IOException e) {
            logger.error(errorMessage, e);
        }
    }

    @Nonnull
    public static String remoteVideoForTest(IDevice device, TestIdentifier test) {
        return remoteFileForTest(device, videoFileName(test));
    }

    @Nonnull
    private static String remoteFileForTest(IDevice device, String filename) {
        return getForkDirectory(device) + "/" + filename;
    }

    @Nonnull
    private static String getCoverageDirectory(IDevice device) {
        return getForkDirectory(device) + "/coverage";
    }

    @Nonnull
    private static String getForkDirectory(IDevice device) {
        String externalStorage = device.getMountPoint(IDevice.MNT_EXTERNAL_STORAGE);
        if (externalStorage != null) {
            return externalStorage + "/fork";
        } else {
            return "/sdcard/fork";
        }
    }

    private static String videoFileName(TestIdentifier test) {
        return String.format("%s-%s.mp4", test.getClassName(), test.getTestName());
    }
}
