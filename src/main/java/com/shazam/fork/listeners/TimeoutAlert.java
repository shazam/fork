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
package com.shazam.fork.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * After some seconds of inactivity, prints a warning with the serial numbers of the pending devices.
 */
public class TimeoutAlert implements SwimlaneConsoleLogger.StatusChangedListener {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutAlert.class);
    private final Timer timer = new Timer("TestTimeoutAlert", true);
    private final int millis;
    private TimerTask task;

    public TimeoutAlert(int millis) {
        this.millis = millis;
    }

    @Override
    public void complete() {
        timer.cancel();
    }

    @Override
    public synchronized void onStatusChanged(SwimlaneConsoleLogger statusHolder) {
        if (task != null) {
            task.cancel();
        }
        task = alertTask(statusHolder);
        timer.schedule(task, millis);
    }

    private TimerTask alertTask(final SwimlaneConsoleLogger statusHolder) {
        return new TimerTask() {
            @Override
            public void run() {
                String pendingDevices = statusHolder.pendingDevices();
                logger.warn("Device {} pending after {} seconds of inactivity", pendingDevices, millis/1000);
                if ("".equals(pendingDevices)) {
                    int aborting = 15;
                    logger.warn("Apparently, no devices pending. Aborting in {}sec", aborting);
                    try {
                        Thread.sleep(aborting * 1000);
                    } catch (InterruptedException ignored) {
                    }
                    System.exit(1);
                }
            }
        };
    }
}
