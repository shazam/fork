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

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.*;
import com.shazam.fork.runner.listeners.ForkXmlTestRunListener;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.shazam.fork.Utils.namedExecutor;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.runner.listeners.TestRunListenersFactory.getForkXmlTestRunListener;

public class PoolTestRunner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(PoolTestRunner.class);
    public static final String DROPPED_BY = "DroppedBy-";

    private final Configuration configuration;
    private final FileManager fileManager;
    private final Pool pool;
    private final Queue<TestClass> testClasses;
    private final CountDownLatch poolCountDownLatch;
    private final DeviceTestRunnerFactory deviceTestRunnerFactory;
    private final ProgressReporter progressReporter;

    public PoolTestRunner(Configuration configuration,
                          FileManager fileManager,
                          DeviceTestRunnerFactory deviceTestRunnerFactory, Pool pool,
                          Queue<TestClass> testClasses,
                          CountDownLatch poolCountDownLatch,
                          ProgressReporter progressReporter) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        this.pool = pool;
        this.testClasses = testClasses;
        this.poolCountDownLatch = poolCountDownLatch;
        this.deviceTestRunnerFactory = deviceTestRunnerFactory;
        this.progressReporter = progressReporter;
    }

    public void run() {
        ExecutorService concurrentDeviceExecutor = null;
        String poolName = pool.getName();
        try {
            int devicesInPool = pool.size();
            concurrentDeviceExecutor = namedExecutor(devicesInPool, "DeviceExecutor-%d");
            CountDownLatch deviceCountDownLatch = new CountDownLatch(devicesInPool);
            logger.info("Pool {} started", poolName);
            for (Device device : pool.getDevices()) {
                Runnable deviceTestRunner = deviceTestRunnerFactory.createDeviceTestRunner(pool, testClasses,
                        deviceCountDownLatch, device, progressReporter);
                concurrentDeviceExecutor.execute(deviceTestRunner);
            }
            deviceCountDownLatch.await();
        } catch (InterruptedException e) {
            logger.warn("Pool {} was interrupted while running", poolName);
        } finally {
            failAnyDroppedClasses(pool, testClasses);
            if (concurrentDeviceExecutor != null) {
                concurrentDeviceExecutor.shutdown();
            }
            logger.info("Pool {} finished", poolName);
            poolCountDownLatch.countDown();
            logger.info("Pools remaining: {}", poolCountDownLatch.getCount());
        }
    }

    /**
     * Only generate XML files for dropped classes the console listener and logcat listeners aren't relevant to
     * dropped tests.
     * <p/>
     * In particular, not triggering the console listener will probably make the flaky report better.
     */
    private void failAnyDroppedClasses(Pool pool, Queue<TestClass> testClassQueue) {
        HashMap<String, String> emptyHash = new HashMap<>();
        TestClass nextTest;
        while ((nextTest = testClassQueue.poll()) != null) {
            String className = nextTest.getName();
            String poolName = pool.getName();
            Device failedTestsDevice = aDevice().withSerial(DROPPED_BY + poolName).build();
            ForkXmlTestRunListener xmlGenerator = getForkXmlTestRunListener(fileManager, configuration.getOutput(),
                    pool, failedTestsDevice, nextTest);

            Collection<TestMethod> methods = nextTest.getUnignoredMethods();
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
