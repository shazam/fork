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
import com.shazam.fork.system.CollectingShellOutputReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class ScreenRecorderStopper {
    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorderStopper.class);
    private static final int SCREENRECORD_KILL_ATTEMPTS = 5;
    private static final int PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 300;
    private final NullOutputReceiver nullOutputReceiver = new NullOutputReceiver();
    private final IDevice deviceInterface;
    private boolean hasFailed;

    ScreenRecorderStopper(IDevice deviceInterface) {
        this.deviceInterface = deviceInterface;
    }

    /**
     * Stops all running screenrecord processes.
     */
    public void stopScreenRecord(boolean hasFailed) {
        this.hasFailed = hasFailed;
        boolean hasKilledScreenRecord = true;
        int tries = 0;
        while (hasKilledScreenRecord && tries++ < SCREENRECORD_KILL_ATTEMPTS) {
            hasKilledScreenRecord = attemptToGracefullyKillScreenRecord();
            pauseBetweenProcessKill();
        }
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    private boolean attemptToGracefullyKillScreenRecord() {
        CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
        try {
            deviceInterface.executeShellCommand("ps |grep screenrecord", receiver);
            String pid = extractPidOfScreenrecordProcess(receiver);
            if (isNotBlank(pid)) {
                logger.trace("Killing PID {} on {}", pid, deviceInterface.getSerialNumber());
                deviceInterface.executeShellCommand("kill -2 " + pid, nullOutputReceiver);
                return true;
            }
            logger.trace("Did not kill any screen recording process");
        } catch (Exception e) {
            logger.error("Error while killing recording processes", e);
        }
        return false;
    }

    private void pauseBetweenProcessKill() {
        try {
            Thread.sleep(PAUSE_BETWEEN_RECORDER_PROCESS_KILL);
        } catch (InterruptedException ignored) {
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
