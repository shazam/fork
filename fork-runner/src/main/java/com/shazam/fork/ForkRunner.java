/*
 * Copyright 2019 Apple Inc.
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

import com.shazam.fork.aggregator.AggregatedTestResult;
import com.shazam.fork.aggregator.Aggregator;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.pooling.NoDevicesForPoolException;
import com.shazam.fork.pooling.NoPoolLoaderConfiguredException;
import com.shazam.fork.pooling.PoolLoader;
import com.shazam.fork.runner.PoolTestRunnerFactory;
import com.shazam.fork.runner.ProgressReporter;
import com.shazam.fork.runner.ProgressReporterFactory;
import com.shazam.fork.suite.NoTestCasesFoundException;
import com.shazam.fork.suite.TestSuiteLoader;
import com.shazam.fork.summary.OutcomeAggregator;
import com.shazam.fork.summary.SummaryGeneratorHook;
import com.shazam.fork.summary.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitTerminationUninterruptibly;
import static com.shazam.fork.Utils.namedExecutor;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ForkRunner {
    private static final Logger logger = LoggerFactory.getLogger(ForkRunner.class);

    private final PoolLoader poolLoader;
    private final TestSuiteLoader testClassLoader;
    private final PoolTestRunnerFactory poolTestRunnerFactory;
    private final ProgressReporterFactory progressReporterFactory;
    private final SummaryGeneratorHook summaryGeneratorHook;
    private final OutcomeAggregator outcomeAggregator;
    private final Aggregator aggregator;

    public ForkRunner(PoolLoader poolLoader,
                      TestSuiteLoader testClassLoader,
                      PoolTestRunnerFactory poolTestRunnerFactory,
                      ProgressReporterFactory progressReporterFactory,
                      SummaryGeneratorHook summaryGeneratorHook,
                      OutcomeAggregator outcomeAggregator, Aggregator aggregator) {
        this.poolLoader = poolLoader;
        this.testClassLoader = testClassLoader;
        this.poolTestRunnerFactory = poolTestRunnerFactory;
        this.progressReporterFactory = progressReporterFactory;
        this.summaryGeneratorHook = summaryGeneratorHook;
        this.outcomeAggregator = outcomeAggregator;
        this.aggregator = aggregator;
    }

    public boolean run() {
        try {
            Collection<Pool> pools = poolLoader.loadPools();

            Collection<TestCaseEvent> testCases = testClassLoader.loadTestSuite();
            summaryGeneratorHook.registerHook(pools, testCases);

            executeTests(pools, testCases);


            AggregatedTestResult aggregatedTestResult = aggregator.aggregateTestResults(pools, testCases);
            if (!aggregatedTestResult.getFatalCrashedTests().isEmpty()) {
                reportMissingTests(aggregatedTestResult);
                System.out.println("Scheduling their re-execution");

                Collection<TestCaseEvent> fatalCrashedTestCases =
                        findFatalCrashedTestCases(testCases, aggregatedTestResult.getFatalCrashedTests());
                executeTests(pools, fatalCrashedTestCases);

                aggregatedTestResult = aggregator.aggregateTestResults(pools, testCases);

                if (!aggregatedTestResult.getFatalCrashedTests().isEmpty()) {
                    reportMissingTests(aggregatedTestResult);
                }
            }

            boolean isSuccessful = outcomeAggregator.aggregate(aggregatedTestResult);
            logger.info("Overall success: " + isSuccessful);

            summaryGeneratorHook.generateSummary(isSuccessful, aggregatedTestResult);

            return isSuccessful;
        } catch (NoPoolLoaderConfiguredException | NoDevicesForPoolException e) {
            logger.error("Configuring devices and pools failed", e);
            return false;
        } catch (NoTestCasesFoundException e) {
            logger.error("Error when trying to find test classes", e);
            return false;
        } catch (Exception e) {
            logger.error("Error while Fork was executing", e);
            return false;
        }
    }

    private void executeTests(
            Collection<Pool> pools,
            Collection<TestCaseEvent> testCases
    ) {
        ProgressReporter progressReporter = progressReporterFactory.createProgressReporter();
        progressReporter.start();

        ExecutorService poolExecutor = null;
        try {
            poolExecutor = namedExecutor(pools.size(), "PoolExecutor-%d");

            for (Pool pool : pools) {
                Runnable poolTestRunner = poolTestRunnerFactory.createPoolTestRunner(
                        pool,
                        testCases,
                        progressReporter
                );
                poolExecutor.submit(poolTestRunner);
            }

            poolExecutor.shutdown();
            awaitTerminationUninterruptibly(poolExecutor);
        } finally {
            progressReporter.stop();

            if (poolExecutor != null && !poolExecutor.isTerminated()) {
                poolExecutor.shutdownNow();
                awaitTerminationUninterruptibly(poolExecutor);
            }
        }

    }

    private static void reportMissingTests(AggregatedTestResult aggregatedTestResult) {
        System.out.println("Test reports are not found for some tests");
        System.out.println("Affected tests: " + getAffectedTests(aggregatedTestResult));
    }

    private static Collection<String> getAffectedTests(AggregatedTestResult aggregatedTestResult) {
        return aggregatedTestResult.getFatalCrashedTests().stream()
                .map(TestResult::getTestFullName)
                .collect(toList());
    }

    private static Collection<TestCaseEvent> findFatalCrashedTestCases(Collection<TestCaseEvent> testCases,
                                                                       Collection<TestResult> fatalCrashedTests) {
        Set<String> tests = fatalCrashedTests.stream()
                .map(TestResult::getTestFullName)
                .collect(toSet());
        return testCases.stream()
                .filter(event -> tests.contains(event.getTestFullName()))
                .collect(toList());
    }
}
