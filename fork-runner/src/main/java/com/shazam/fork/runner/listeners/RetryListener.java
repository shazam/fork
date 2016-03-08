/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.ProgressReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

import javax.annotation.Nonnull;

import static com.beust.jcommander.internal.Lists.newLinkedList;
import static com.google.common.base.Preconditions.checkNotNull;

public class RetryListener extends NoOpITestRunListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryListener.class);

    @Nonnull
    private final Device device;
    @Nonnull
    private final Queue<TestIdentifier> failedTests = newLinkedList();
    @Nonnull
    private final Queue<TestCaseEvent> queueOfTestsInPool;
    @Nonnull
    private final TestCaseEvent currentTestCaseEvent;
    private ProgressReporter progressReporter;

    public RetryListener(@Nonnull Device device,
                         @Nonnull Queue<TestCaseEvent> queueOfTestsInPool,
                         @Nonnull TestCaseEvent currentTestCaseEvent,
                         ProgressReporter progressReporter) {
        checkNotNull(device);
        checkNotNull(queueOfTestsInPool);
        checkNotNull(currentTestCaseEvent);
        this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.currentTestCaseEvent = currentTestCaseEvent;
        this.progressReporter = progressReporter;
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        failedTests.add(test);
        progressReporter.recordFailedTestCase(device, new TestCaseEvent(test.getTestName(), test.getClassName(), false));
        if (progressReporter.retryWatchdog().allowRetry()) {
            queueOfTestsInPool.add(currentTestCaseEvent);
            logger.info("Test " + test.toString() + " enqueued again into device." + device.getSerial());
        } else {
            logger.debug("Test " + test.toString() + " failed on device " + device.getSerial() + " but retry is not allowed.");
        }
    }
}
