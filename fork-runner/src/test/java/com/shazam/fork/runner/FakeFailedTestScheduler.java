package com.shazam.fork.runner;

import com.shazam.fork.model.TestCaseEvent;

public class FakeFailedTestScheduler implements FailedTestScheduler {
    private boolean result;

    private FakeFailedTestScheduler() {
    }

    public static FakeFailedTestScheduler fakeFailedTestScheduler() {
        return new FakeFailedTestScheduler();
    }

    public FakeFailedTestScheduler thatReturns(boolean result) {
        this.result = result;
        return this;
    }

    @Override
    public boolean rescheduleTestExecution(TestCaseEvent testCaseEvent) {
        return result;
    }
}
