package com.shazam.fork.model;

public class TestCaseEventCounter extends Counter<TestCaseEvent> {

    public static final TestCaseEventCounter EMPTY = new TestCaseEventCounter(null, 0);

    public TestCaseEventCounter(TestCaseEvent type, int initialCount) {
        super(type, initialCount);
    }

    public TestCaseEventCounter withIncreasedCount() {
        super.increaseCount();
        return this;
    }
}
