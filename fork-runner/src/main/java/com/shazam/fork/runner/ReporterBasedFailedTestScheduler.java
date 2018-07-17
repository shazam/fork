package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import java.util.Queue;

public class ReporterBasedFailedTestScheduler implements FailedTestScheduler {
    private final ProgressReporter progressReporter;
    private final Pool pool;
    private final Queue<TestCaseEvent> queueOfTestsInPool;

    public ReporterBasedFailedTestScheduler(ProgressReporter progressReporter,
                                            Pool pool,
                                            Queue<TestCaseEvent> queueOfTestsInPool) {
        this.progressReporter = progressReporter;
        this.pool = pool;
        this.queueOfTestsInPool = queueOfTestsInPool;
    }

    @Override
    public boolean rescheduleTestExecution(TestCaseEvent testCaseEvent) {
        progressReporter.recordFailedTestCase(pool, testCaseEvent);
        if (progressReporter.requestRetry(pool, testCaseEvent)) {
            queueOfTestsInPool.add(testCaseEvent);
            return true;
        }
        return false;
    }
}
