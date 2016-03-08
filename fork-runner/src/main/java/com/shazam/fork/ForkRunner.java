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
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.shazam.fork.injector.ConfigurationInjector;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestMethod;
import com.shazam.fork.pooling.NoDevicesForPoolException;
import com.shazam.fork.pooling.PoolLoader;
import com.shazam.fork.runner.FailureAccumulator;
import com.shazam.fork.runner.FailureAccumulatorImpl;
import com.shazam.fork.runner.PoolTestRunner;
import com.shazam.fork.runner.PoolTestRunnerFactory;
import com.shazam.fork.runner.ProgressReporter;
import com.shazam.fork.suite.TestClassLoader;
import com.shazam.fork.suite.TestClassScanningException;
import com.shazam.fork.summary.SummaryGeneratorHook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import static com.google.common.collect.FluentIterable.from;
import static com.shazam.fork.Utils.namedExecutor;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;

public class ForkRunner {
    private static final Logger logger = LoggerFactory.getLogger(ForkRunner.class);

    private final PoolLoader poolLoader;
    private final TestClassLoader testClassLoader;
    private final PoolTestRunnerFactory poolTestRunnerFactory;
    private final ProgressReporter progressReporter;
    private final SummaryGeneratorHook summaryGeneratorHook;
    private final FailureAccumulatorImpl failureAccumulator;

    public ForkRunner(PoolLoader poolLoader,
                      TestClassLoader testClassLoader,
                      PoolTestRunnerFactory poolTestRunnerFactory,
                      ProgressReporter progressReporter,
                      SummaryGeneratorHook summaryGeneratorHook, FailureAccumulatorImpl failureAccumulator) {
        this.poolLoader = poolLoader;
        this.testClassLoader = testClassLoader;
        this.poolTestRunnerFactory = poolTestRunnerFactory;
        this.progressReporter = progressReporter;
        this.summaryGeneratorHook = summaryGeneratorHook;
        this.failureAccumulator = failureAccumulator;
    }

    private List<TestClass> convertToTestClass(Collection<TestIdentifier> testIdentifiers) {

        List<TestClass> result = Lists.newArrayList();

        for (TestIdentifier testIdentifier : testIdentifiers) {
            TestMethod testMethod = TestMethod.Builder.testMethod()
                    .withName(testIdentifier.getTestName()).build();

            TestClass testClass = TestClass.Builder.testClass()
                    .withName(testIdentifier.getClassName())
                    .withMethods(Lists.newArrayList(testMethod))
                    .build();
            result.add(testClass);

        }
        return result;
    }

    public boolean run() {
        ExecutorService poolExecutor = null;
        try {
            Collection<Pool> pools = poolLoader.loadPools();
            int numberOfPools = pools.size();
            CountDownLatch poolCountDownLatch = new CountDownLatch(numberOfPools);
            poolExecutor = namedExecutor(numberOfPools, "PoolExecutor-%d");

            List<TestCaseEvent> testCases = testClassLoader.loadTestClasses();
            summaryGeneratorHook.registerHook(pools, testCases);

            progressReporter.start();
            for (Pool pool : pools) {
                PoolTestRunner poolTestRunner = poolTestRunnerFactory.createPoolTestRunner(pool, testCases,
                        poolCountDownLatch, progressReporter);
                poolExecutor.execute(poolTestRunner);
            }
            poolCountDownLatch.await();

            Configuration configuration = ConfigurationInjector.configuration();
            if(!failureAccumulator.isEmpty()){
                logger.info("Re-run failing tests.");
                rerunFailures(poolExecutor, pools);
            }

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

    private void rerunFailures(ExecutorService poolExecutor, Collection<Pool> pools) throws InterruptedException {
        for(final Device device : failureAccumulator.getFailures().keySet()){
            Collection<TestIdentifier> testIdentifiers = failureAccumulator.getFailures().get(device);

            Optional<Pool> poolWithDevice = from(pools).firstMatch(new Predicate<Pool>() {
                @Override
                public boolean apply(@Nullable Pool input) {
                    return input != null && input.getDevices().contains(device);
                }
            });
            CountDownLatch retryCountDownLatch = new CountDownLatch(1);
            if(poolWithDevice.isPresent()){
                Pool newPoolWithSingleDevice = aDevicePool().addDevice(device)
                        .withName(poolWithDevice.get().getName())
                        .build();

                PoolTestRunner poolTestRunner = poolTestRunnerFactory.createPoolTestRunner(newPoolWithSingleDevice,
                        convertToTestClass(testIdentifiers),
                        retryCountDownLatch,
                        progressReporter,
                        new FailureAccumulator.NoOpFailureAccumulator()
                );
                poolExecutor.execute(poolTestRunner);
                retryCountDownLatch.await();
            }
        }
    }
}
