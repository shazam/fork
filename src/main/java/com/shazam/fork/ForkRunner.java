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

import com.shazam.fork.io.FileManager;
import com.shazam.fork.model.*;
import com.shazam.fork.pooling.DevicePoolLoader;
import com.shazam.fork.runtime.SwimlaneConsoleLogger;
import com.shazam.fork.summary.*;
import com.shazam.fork.system.DeviceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.shazam.fork.Utils.namedExecutor;

public class ForkRunner {
    private static final Logger logger = LoggerFactory.getLogger(ForkRunner.class);

    private final RuntimeConfiguration runtimeConfiguration;
    private final DeviceLoader deviceLoader;
    private final DevicePoolLoader poolLoader;
    private final TestClassScanner testClassScanner;
    private final TestClassFilter testClassFilter;
    private final DevicePoolRunner devicePoolRunner;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final SummaryPrinter summaryPrinter;
    private final FileManager fileManager;

    public ForkRunner(RuntimeConfiguration runtimeConfiguration,
                      DeviceLoader deviceLoader,
                      DevicePoolLoader poolLoader,
                      TestClassScanner testClassScanner,
                      TestClassFilter testClassFilter,
                      DevicePoolRunner devicePoolRunner,
                      SwimlaneConsoleLogger swimlaneConsoleLogger,
                      SummaryPrinter summaryPrinter,
                      FileManager fileManager) {
        this.runtimeConfiguration = runtimeConfiguration;
        this.deviceLoader = deviceLoader;
        this.poolLoader = poolLoader;
        this.testClassScanner = testClassScanner;
		this.testClassFilter = testClassFilter;
        this.devicePoolRunner = devicePoolRunner;
        this.swimlaneConsoleLogger = swimlaneConsoleLogger;
        this.summaryPrinter = summaryPrinter;
        this.fileManager = fileManager;
    }

	public boolean run() {
		ExecutorService poolExecutor = null;
		try {
			// Get all connected devices & pools, fail if none
            Devices devices = deviceLoader.loadDevices();
            if (devices.getDevices().isEmpty()) {
                logger.error("No devices found, so marking as failure");
                return false;
            }

			Collection<DevicePool> devicePools = poolLoader.loadPools(devices);
			if (devicePools.isEmpty()) {
				logger.error("No device pools found, so marking as failure");
				return false;
			}

			List<TestClass> allTestClasses = testClassScanner.scanForTestClasses();
            final List<TestClass> testClasses = testClassFilter.anyUserFilter(allTestClasses);

			int numberOfPools = devicePools.size();
			final CountDownLatch poolCountDownLatch = new CountDownLatch(numberOfPools);
            poolExecutor = namedExecutor(numberOfPools, "PoolExecutor-%d");

			// Only need emergency shutdown hook once tests have started.
			ReportGeneratorHook reportGeneratorHook = new ReportGeneratorHook(runtimeConfiguration, fileManager,
                    devicePools, testClasses, summaryPrinter);
			Runtime.getRuntime().addShutdownHook(reportGeneratorHook);

			for (final DevicePool devicePool : devicePools) {
				poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Queue<TestClass> testQueue = new LinkedList<>(testClasses);
                            devicePoolRunner.runTestsOnDevicePool(devicePool, testQueue);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            poolCountDownLatch.countDown();
                            logger.info("Pools remaining: " + poolCountDownLatch.getCount());
                        }
                    }
                });
			}
			poolCountDownLatch.await();
			swimlaneConsoleLogger.complete();

			Summary summary = reportGeneratorHook.generateReportOnlyOnce();
			boolean overallSuccess = summary != null && new OutcomeAggregator().aggregate(summary);
			logger.info("Overall success: " + overallSuccess);

			return overallSuccess;
		} catch (Exception e) {
            logger.error("Error while Fork runner was executing", e);
			return false;
		} finally {
			if (poolExecutor != null) {
				poolExecutor.shutdown();
			}
		}
	}
}
