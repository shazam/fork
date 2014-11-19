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
package com.shazam.fork.io;

import com.android.ddmlib.IDevice;
import com.shazam.fork.system.NoOpIShellOutputReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Could we improve behaviour for different types of ADB exceptions?
public class RemoteFileManager {
    private static final Logger logger = LoggerFactory.getLogger(RemoteFileManager.class);
    private static final NoOpIShellOutputReceiver NO_OP_RECEIVER = new NoOpIShellOutputReceiver();

    public static void removeRemotePath(IDevice device, String remotePath) {
        try {
            device.executeShellCommand("rm " + remotePath, NO_OP_RECEIVER);
        } catch (Exception e) {
            logger.error("Could not delete remote file(s): " + remotePath, e);
        }
    }

    public static void createRemoteDirectory(IDevice device, String remoteDirectory) {
        try {
            device.executeShellCommand("mkdir " + remoteDirectory, NO_OP_RECEIVER);
        } catch (Exception e) {
            logger.error("Could not create remote directory: " + remoteDirectory, e);
        }
    }

    public static void removeRemoteDirectory(IDevice device, String remoteDirectory) {
        try {
            device.executeShellCommand("rm -r " + remoteDirectory, NO_OP_RECEIVER);
        } catch (Exception e) {
            logger.error("Could not delete remote directory: " + remoteDirectory, e);
        }
    }
}
