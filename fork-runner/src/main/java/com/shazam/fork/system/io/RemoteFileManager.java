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
package com.shazam.fork.system.io;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.TestIdentifier;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteFileManager {

    private static final Logger logger = LoggerFactory.getLogger(RemoteFileManager.class);
    private static final String FORK_DIRECTORY = "/sdcard/fork";
    private static final NullOutputReceiver NO_OP_RECEIVER = new NullOutputReceiver();
    private static final String COVERAGE_DIRECTORY = FORK_DIRECTORY + "/coverage";

    private RemoteFileManager() {}

    public static void removeRemotePath(IDevice device, String remotePath) {
        executeCommand(device, "rm " + remotePath, "Could not delete remote file(s): " + remotePath);
    }

    public static void createCoverageDirectory(IDevice device) {
        executeCommand(device, "mkdir " + COVERAGE_DIRECTORY,
                       "Could not create remote directory: " + COVERAGE_DIRECTORY);
    }

    public static String getCoverageFileName(TestIdentifier testIdentifier) {
        return COVERAGE_DIRECTORY + "/" +testIdentifier.toString() + ".ec";
    }

    public static void createRemoteDirectory(IDevice device) {
        executeCommand(device, "mkdir " + FORK_DIRECTORY, "Could not create remote directory: " + FORK_DIRECTORY);
    }

    public static void removeRemoteDirectory(IDevice device) {
        executeCommand(device, "rm -r " + FORK_DIRECTORY, "Could not delete remote directory: " + FORK_DIRECTORY);
    }

    private static void executeCommand(IDevice device, String command, String errorMessage) {
        try {
            device.executeShellCommand(command, NO_OP_RECEIVER);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            logger.error(errorMessage, e);
        }
    }

    public static String remoteVideoForTest(TestIdentifier test) {
        return remoteFileForTest(videoFileName(test));
    }

    private static String remoteFileForTest(String filename) {
        return FORK_DIRECTORY + "/" + filename;
    }

    private static String videoFileName(TestIdentifier test) {
        return String.format("%s-%s.mp4", test.getClassName(), test.getTestName());
    }
}
