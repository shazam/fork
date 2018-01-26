package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.TestCaseEvent;

public class FakeTestRetryer implements TestRetryer {
    private boolean result;

    private FakeTestRetryer() {
    }

    public static FakeTestRetryer fakeTestRetryer() {
        return new FakeTestRetryer();
    }

    public FakeTestRetryer thatReturns(boolean result) {
        this.result = result;
        return this;
    }

    @Override
    public boolean rescheduleTestExecution(TestIdentifier testIdentifier, TestCaseEvent testCaseEvent) {
        return result;
    }
}
