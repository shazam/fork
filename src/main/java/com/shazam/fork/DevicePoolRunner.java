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
package com.shazam.fork;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.gson.Gson;
import com.shazam.fork.io.FileManager;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.DevicePool;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestMethod;
import com.shazam.fork.runtime.ForkXmlTestRunListener;
import com.shazam.fork.runtime.SwimlaneConsoleLogger;
import com.shazam.fork.system.Installer;
import com.shazam.fork.system.NoDevicesForPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.shazam.fork.TestSuiteRunnerTask.getForkXmlTestRunListener;
import static com.shazam.fork.Utils.namedExecutor;
import static java.lang.String.format;

public class DevicePoolRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevicePoolRunner.class);
	public static final String DROPPED_BY = "DroppedBy-";

    private final Configuration configuration;
    private final Gson gson;
    private final Installer installer;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final FileManager fileManager;

    public DevicePoolRunner(Configuration configuration, Gson gson, Installer installer, FileManager fileManager,
                            SwimlaneConsoleLogger swimlaneConsoleLogger) {
        this.configuration = configuration;
        this.gson = gson;
        this.fileManager = fileManager;
        this.installer = installer;
		this.swimlaneConsoleLogger = swimlaneConsoleLogger;
	}

	public void runTestsOnDevicePool(DevicePool devicePool, TestClassProvider testsProvider) throws NoDevicesForPoolException,
            InterruptedException {
		ExecutorService concurrentDeviceExecutor = null;
		try {
			if (devicePool.isEmpty()) {
				throw new NoDevicesForPoolException(format("No connected devices in pool %s", devicePool.getName()));
			}
			int devicesInPool = devicePool.size();
			concurrentDeviceExecutor = namedExecutor(devicesInPool, "DeviceExecutor-%d");
			CountDownLatch deviceInPoolCountDownLatch = new CountDownLatch(devicesInPool);
			for (Device device : devicePool.getDevices()) {
				concurrentDeviceExecutor.execute(new TestSuiteRunnerTask(
                        configuration,
                        gson,
                        installer,
                        fileManager,
                        devicePool.getName(),
                        device,
						testsProvider,
						deviceInPoolCountDownLatch,
                        swimlaneConsoleLogger));
			}
			deviceInPoolCountDownLatch.await();
		} finally {
			failAnyDroppedClasses(devicePool, testsProvider);
			if (concurrentDeviceExecutor != null) {
				concurrentDeviceExecutor.shutdown();
			}
            logger.info("Pool {} finished", devicePool.getName());
		}
	}

	/**
     * Only generate XML files for dropped classes the console listener and logcat listeners aren't relevant to
     * dropped tests.
	 *
	 *  In particular, not triggering the console listener will probably make the flaky report better.
	 */
	private void failAnyDroppedClasses(DevicePool devicePool, TestClassProvider testsProvider) {
		HashMap<String, String> emptyHash = new HashMap<>();
		for (TestClass nextTest; (nextTest = testsProvider.getNextTest()) != null;) {
			String className = nextTest.getClassName();
			String poolName = devicePool.getName();
			ForkXmlTestRunListener xmlGenerator = getForkXmlTestRunListener(fileManager, configuration.getOutput(),
                    poolName, DROPPED_BY + poolName, nextTest);

			List<TestMethod> methods = nextTest.getUnsuppressedMethods();
			xmlGenerator.testRunStarted(poolName, methods.size());
			for (TestMethod method : methods) {
				String methodName = method.getName();
				TestIdentifier identifier = new TestIdentifier(className, methodName);
				xmlGenerator.testStarted(identifier);
				xmlGenerator.testFailed(identifier, poolName + " DROPPED");
				xmlGenerator.testEnded(identifier, emptyHash);
			}
			xmlGenerator.testRunFailed("DROPPED BY " + poolName);
			xmlGenerator.testRunEnded(0, emptyHash);
		}
	}
}
