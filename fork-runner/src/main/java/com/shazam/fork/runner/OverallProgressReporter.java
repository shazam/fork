/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shazam.fork.utils.Utils.millisBetweenNanoTimes;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;

/**
 * Reports for stats on all the pools.
 */
public class OverallProgressReporter implements ProgressReporter {

    private final Map<Pool, PoolProgressTracker> poolProgressTrackers;
    private final RetryWatchdog retryWatchdog;
    private final PoolTestCaseAccumulator failedTestCasesAccumulator;
    private long startOfTests;
    private long endOfTests;

    public OverallProgressReporter(int totalAllowedRetryQuota,
                                   int retryPerTestCaseQuota,
                                   Map<Pool, PoolProgressTracker> poolProgressTrackers,
                                   PoolTestCaseAccumulator failedTestCasesAccumulator) {
        this.retryWatchdog = new RetryWatchdog(totalAllowedRetryQuota, retryPerTestCaseQuota);
        this.poolProgressTrackers = poolProgressTrackers;
        this.failedTestCasesAccumulator = failedTestCasesAccumulator;
    }

    @Override
    public void start() {
        startOfTests = nanoTime();
    }

    @Override
    public void stop() {
        endOfTests = nanoTime();
    }

    @Override
    public void addPoolProgress(Pool pool, PoolProgressTracker poolProgressTracker) {
        poolProgressTrackers.put(pool, poolProgressTracker);
    }

    @Override
    public PoolProgressTracker getProgressTrackerFor(Pool pool) {
        return poolProgressTrackers.get(pool);
    }

    @Override
    public long millisSinceTestsStarted() {
        if (endOfTests == 0) {
            return millisSinceNanoTime(startOfTests);
        }
        return millisBetweenNanoTimes(startOfTests, endOfTests);
    }

    @Override
    public int getFailures() {
        int sum = 0;
        for (PoolProgressTracker value : poolProgressTrackers.values()) {
            sum += value.getNumberOfFailedTests();
        }
        return sum;
    }

    @Override
    public float getProgress() {
        float size = poolProgressTrackers.size();
        float progress = 0;

        for (PoolProgressTracker value : poolProgressTrackers.values()) {
            progress += value.getProgress();
        }

        return progress / size;
    }

    public boolean requestRetry(Pool pool, TestCaseEvent testCase) {
        boolean result = retryWatchdog.requestRetry(failedTestCasesAccumulator.getCount(testCase));
        if (result && poolProgressTrackers.containsKey(pool)) {
            poolProgressTrackers.get(pool).trackTestEnqueuedAgain();
        }
        return result;
    }

    @Override
    public void recordFailedTestCase(Pool pool, TestCaseEvent testCase) {
        failedTestCasesAccumulator.record(pool, testCase);
    }

    @Override
    public int getTestFailuresCount(Pool pool, TestCaseEvent testCase) {
        return failedTestCasesAccumulator.getCount(pool, testCase);
    }

    private class RetryWatchdog {
        private final Logger logger = LoggerFactory.getLogger(RetryWatchdog.class);
        private final int maxRetryPerTestCaseQuota;
        private final AtomicInteger totalAllowedRetryLeft;
        private final StringBuilder logBuilder = new StringBuilder();

        public RetryWatchdog(int totalAllowedRetryQuota, int retryPerTestCaseQuota) {
            totalAllowedRetryLeft = new AtomicInteger(totalAllowedRetryQuota);
            maxRetryPerTestCaseQuota = retryPerTestCaseQuota;
        }

        public boolean requestRetry(int currentSingleTestCaseFailures) {
            boolean totalAllowedRetryAvailable = totalAllowedRetryAvailable();
            boolean singleTestAllowed = currentSingleTestCaseFailures <= maxRetryPerTestCaseQuota;
            boolean result = totalAllowedRetryAvailable && singleTestAllowed;

            log(currentSingleTestCaseFailures, singleTestAllowed, result);
            return result;
        }

        private boolean totalAllowedRetryAvailable() {
            return totalAllowedRetryLeft.get() > 0 && totalAllowedRetryLeft.getAndDecrement() >= 0;
        }

        private void log(int testCaseFailures, boolean singleTestAllowed, boolean result) {
            if(logger.isDebugEnabled()) {
                logBuilder.setLength(0); //clean up.
                logBuilder.append("Retry requested ")
                        .append(result ? " and allowed. " : " but not allowed. ")
                        .append("Total retry left :").append(totalAllowedRetryLeft.get())
                        .append(" and Single Test case retry left: ")
                        .append(singleTestAllowed ? maxRetryPerTestCaseQuota - testCaseFailures : 0);
                logger.debug(logBuilder.toString());
            }
        }
    }
}
