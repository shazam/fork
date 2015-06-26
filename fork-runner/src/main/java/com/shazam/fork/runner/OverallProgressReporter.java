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

import com.shazam.fork.model.Pool;

import java.util.HashMap;
import java.util.Map;

import static com.shazam.fork.utils.Utils.millisBetweenNanoTimes;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;

/**
 * Reports for stats on all the pools.
 */
public class OverallProgressReporter implements ProgressReporter {
    private long startOfTests;
    private long endOfTests;
    private final Map<Pool, PoolProgressTracker> poolProgressTrackers = new HashMap<>();

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

}
