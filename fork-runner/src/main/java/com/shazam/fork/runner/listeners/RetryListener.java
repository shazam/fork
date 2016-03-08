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
import com.shazam.fork.injector.system.FileManagerInjector;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.ProgressReporter;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

public class RetryListener extends NoOpITestRunListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryListener.class);

    @Nonnull
    private final Device device;
    @Nonnull
    private TestIdentifier failedTest;
    @Nonnull
    private final Queue<TestCaseEvent> queueOfTestsInPool;
    @Nonnull
    private final TestCaseEvent currentTestCaseEvent;
    private ProgressReporter progressReporter;
    private FileManager fileManager;
    private Pool pool;

    public RetryListener(@Nonnull Pool pool, @Nonnull Device device,
                         @Nonnull Queue<TestCaseEvent> queueOfTestsInPool,
                         @Nonnull TestCaseEvent currentTestCaseEvent,
                         @Nonnull ProgressReporter progressReporter,
                         FileManager fileManager) {
        checkNotNull(device);
        checkNotNull(queueOfTestsInPool);
        checkNotNull(currentTestCaseEvent);
        checkNotNull(progressReporter);
        checkNotNull(pool);
        this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.currentTestCaseEvent = currentTestCaseEvent;
        this.progressReporter = progressReporter;
        this.pool = pool;
        this.fileManager = fileManager;
    }


    @Override
    public void testFailed(TestIdentifier test, String trace) {
        failedTest = test;
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        super.testRunEnded(elapsedTime, runMetrics);
        if (failedTest != null) {
            progressReporter.recordFailedTestCase(device, new TestCaseEvent(failedTest.getTestName(), failedTest.getClassName(), false));
            if (progressReporter.retryWatchdog().allowRetry()) {
                queueOfTestsInPool.add(currentTestCaseEvent);
                logger.info("Test " + failedTest.toString() + " enqueued again into device." + device.getSerial());
                removeFailureTraceFiles();

            } else {
                logger.debug("Test " + failedTest.toString() + " failed on device " + device.getSerial() + " but retry is not allowed.");
            }
        }
    }

    public void removeFailureTraceFiles( ) {
        final File file = FileManagerInjector.fileManager().getFile(FileType.TEST, pool.getName(), device.getSafeSerial(), failedTest);
        boolean deleted = file.delete();
        if(!deleted){
            logger.warn("Failed to remove file  " + file.getAbsoluteFile() + " for a failed but enqueued again test");
        }
    }
}
