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
package com.shazam.fork.runner;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.*;
import com.shazam.fork.runner.listeners.CoverageListener;
import com.shazam.fork.system.adb.Installer;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import static com.shazam.fork.system.io.RemoteFileManager.*;

public class DeviceTestRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestRunner.class);

    private final Configuration configuration;
    private final FileManager fileManager;
    private final Installer installer;
    private final Pool pool;
    private final Device device;
    private final Queue<TestCaseEvent> queueOfTestsInPool;
    private final CountDownLatch deviceCountDownLatch;
    private final ProgressReporter progressReporter;
    private final TestRunFactory testRunFactory;

    public DeviceTestRunner(Configuration configuration,
                            FileManager fileManager,
                            Installer installer,
                            Pool pool,
                            Device device,
                            Queue<TestCaseEvent> queueOfTestsInPool,
                            CountDownLatch deviceCountDownLatch,
                            ProgressReporter progressReporter,
                            TestRunFactory testRunFactory) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        this.installer = installer;
        this.pool = pool;
        this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.deviceCountDownLatch = deviceCountDownLatch;
        this.progressReporter = progressReporter;
        this.testRunFactory = testRunFactory;
    }

    @Override
    public void run() {
        IDevice deviceInterface = device.getDeviceInterface();
        try {
            DdmPreferences.setTimeOut(30000);
            installer.prepareInstallation(deviceInterface);
            // For when previous run crashed/disconnected and left files behind
            removeRemoteDirectory(deviceInterface);
            createRemoteDirectory(deviceInterface);
            createCoverageDirectory(deviceInterface);
            clearLogcat(deviceInterface);
            testRunFactory.createTestRun(device, pool, progressReporter, queueOfTestsInPool).execute();
            if (configuration.isCoverageEnabled()) {
                CoverageListener.pullCoverageFile(device, pool, fileManager, logger);
            }
        } finally {
            logger.info("Device {} from pool {} finished", device.getSerial(), pool.getName());
            deviceCountDownLatch.countDown();
        }
    }

    private void clearLogcat(final IDevice device) {
        try {
            device.executeShellCommand("logcat -c", new NullOutputReceiver());
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            logger.warn("Could not clear logcat on device: " + device.getSerialNumber(), e);
        }
    }
}
