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

import com.android.ddmlib.IDevice;
import com.shazam.fork.listeners.SwimlaneConsoleLogger;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.system.adb.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import static com.shazam.fork.system.io.RemoteFileManager.createRemoteDirectory;
import static com.shazam.fork.system.io.RemoteFileManager.removeRemoteDirectory;

public class DeviceTestRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestRunner.class);

    private final Installer installer;
    private final String poolName;
    private final Device device;
    private final Queue<TestClass> queueOfTestsInPool;
    private final CountDownLatch deviceCountDownLatch;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final TestRunFactory testRunFactory;

    public DeviceTestRunner(Installer installer,
                            String poolName,
                            Device device,
                            Queue<TestClass> queueOfTestsInPool,
                            CountDownLatch deviceCountDownLatch,
                            SwimlaneConsoleLogger swimlaneConsoleLogger,
                            TestRunFactory testRunFactory) {
        this.installer = installer;
		this.poolName = poolName;
		this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.deviceCountDownLatch = deviceCountDownLatch;
		this.swimlaneConsoleLogger = swimlaneConsoleLogger;
        this.testRunFactory = testRunFactory;
    }

	@Override
	public void run() {
        String serial = device.getSerial();
        IDevice deviceInterface = device.getDeviceInterface();
        try {
            swimlaneConsoleLogger.setCount(serial, poolName, queueOfTestsInPool.size());
            installer.prepareInstallation(deviceInterface);
            // For when previous run crashed/disconnected and left files behind
            removeRemoteDirectory(deviceInterface);
            createRemoteDirectory(deviceInterface);

            TestClass testClass;
            while ((testClass = queueOfTestsInPool.poll()) != null) {
                TestRun testRun = testRunFactory.createTestRun(testClass, device, poolName);
                testRun.execute();
            }
		} finally {
            logger.info("Device {} from pool {} finished", device.getSerial(), poolName);
			deviceCountDownLatch.countDown();
		}
	}
}
