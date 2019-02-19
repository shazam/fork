/*
 * Copyright 2018 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import javax.annotation.Nonnull;

public final class FakeProgressReporter implements ProgressReporter {
    private boolean canBeRescheduled;

    private FakeProgressReporter() {
    }

    @Nonnull
    public static FakeProgressReporter fakeProgressReporter() {
        return new FakeProgressReporter();
    }

    @Nonnull
    public FakeProgressReporter thatAlwaysDisallowTestToBeRescheduled() {
        canBeRescheduled = false;
        return this;
    }

    @Nonnull
    public FakeProgressReporter thatAlwaysAllowTestToBeRescheduled() {
        canBeRescheduled = true;
        return this;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void addPoolProgress(Pool pool, PoolProgressTracker poolProgressTracker) {

    }

    @Override
    public PoolProgressTracker getProgressTrackerFor(Pool pool) {
        return null;
    }

    @Override
    public long millisSinceTestsStarted() {
        return 0;
    }

    @Override
    public int getTestFailures() {
        return 0;
    }

    @Override
    public int getTestRunFailures() {
        return 0;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public boolean requestRetry(Pool pool, TestCaseEvent testCaseEvent) {
        return canBeRescheduled;
    }

    @Override
    public void recordFailedTestCase(Pool pool, TestCaseEvent testCase) {

    }

    @Override
    public int getTestFailuresCount(Pool pool, TestCaseEvent testCase) {
        return 0;
    }
}
