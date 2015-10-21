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

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.pooling.NoDevicesForPoolException;
import com.shazam.fork.pooling.PoolLoader;
import com.shazam.fork.runner.*;
import com.shazam.fork.suite.TestClassLoader;
import com.shazam.fork.suite.TestClassScanningException;
import com.shazam.fork.summary.SummaryGeneratorHook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.shazam.fork.Utils.namedExecutor;

public class ForkRunner {
    private static final Logger logger = LoggerFactory.getLogger(ForkRunner.class);

    private final PoolLoader poolLoader;
    private final TestClassLoader testClassLoader;
    private final PoolTestRunnerFactory poolTestRunnerFactory;
    private final ProgressReporter progressReporter;
    private final SummaryGeneratorHook summaryGeneratorHook;

    public ForkRunner(PoolLoader poolLoader,
                      TestClassLoader testClassLoader,
                      PoolTestRunnerFactory poolTestRunnerFactory,
                      ProgressReporter progressReporter,
                      SummaryGeneratorHook summaryGeneratorHook) {
        this.poolLoader = poolLoader;
        this.testClassLoader = testClassLoader;
        this.poolTestRunnerFactory = poolTestRunnerFactory;
        this.progressReporter = progressReporter;
        this.summaryGeneratorHook = summaryGeneratorHook;
    }

    public boolean run() {
        ExecutorService poolExecutor = null;
        try {
            Collection<Pool> pools = poolLoader.loadPools();
            int numberOfPools = pools.size();
            CountDownLatch poolCountDownLatch = new CountDownLatch(numberOfPools);
            poolExecutor = namedExecutor(numberOfPools, "PoolExecutor-%d");

            List<TestClass> testClasses = testClassLoader.loadTestClasses();
            summaryGeneratorHook.registerHook(pools, testClasses);

            progressReporter.start();
            for (Pool pool : pools) {
                PoolTestRunner poolTestRunner = poolTestRunnerFactory.createPoolTestRunner(pool, testClasses,
                        poolCountDownLatch, progressReporter);
                poolExecutor.execute(poolTestRunner);
            }
            poolCountDownLatch.await();
            progressReporter.stop();

            boolean overallSuccess = summaryGeneratorHook.defineOutcome();
            logger.info("Overall success: " + overallSuccess);
            return overallSuccess;
        } catch (NoDevicesForPoolException e) {
            logger.error("Configuring devices and pools failed", e);
            return false;
        } catch (TestClassScanningException e) {
            logger.error("Error when trying to scan for test classes", e);
            return false;
        } catch (Exception e) {
            logger.error("Error while Fork was executing", e);
            return false;
        } finally {
            if (poolExecutor != null) {
                poolExecutor.shutdown();
            }
        }
    }
}
