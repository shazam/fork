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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.shazam.fork.system.CancellableShellOutputReceiver;
import com.shazam.fork.system.CollectingShellOutputReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class ScreenRecorderStopper {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorderStopper.class);
    private final NullOutputReceiver nullOutputReceiver = new NullOutputReceiver();
    private final IDevice deviceInterface;
    private final CancellableShellOutputReceiver cancellableReceiver;

    ScreenRecorderStopper(IDevice deviceInterface, CancellableShellOutputReceiver cancellableReceiver) {
        this.deviceInterface = deviceInterface;
        this.cancellableReceiver = cancellableReceiver;
    }

    void cancelScreenRecord() {
        cancellableReceiver.cancel();
    }

    void stopScreenRecord() {
        CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
        try {
            deviceInterface.executeShellCommand("ps |grep screenrecord", receiver);
            String pid = extractPidOfScreenrecordProcess(receiver);
            if (isNotBlank(pid)) {
                deviceInterface.executeShellCommand("kill -2 " + pid, nullOutputReceiver);
            }
        } catch (Exception e) {
            logger.error("Error while killing recording processes", e);
        }
    }

    /**
     * The output of ps is of the format:
     * <br><code>USER PID PPID VSIZE RSS WCHAN PC NAME</code>
     * <br>
     *
     * <b>NOTE: For now, we're going to assume there's only one screenrecord process at a time.</b>
     *
     * @param receiver the command receiver
     * @return the string representing the screenrecord process PID
     */
    @Nullable
    private String extractPidOfScreenrecordProcess(CollectingShellOutputReceiver receiver) {
        String output = receiver.getOutput();
        if (isBlank(output)) {
            return null;
        }
        String[] split = output.split("\\s+");
        String pid = split[1];
        logger.trace("Extracted PID {} from output {}", pid, output);
        return pid;
    }

}
