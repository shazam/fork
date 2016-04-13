package com.shazam.fork.model;

import com.google.common.base.Objects;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCaseEventCounter {

    public static final TestCaseEventCounter EMPTY = new TestCaseEventCounter(null, 0);

    private TestCaseEvent testCaseEvent;
    private AtomicInteger count;

    public TestCaseEventCounter(TestCaseEvent testCaseEvent, int initialCount) {
        this.testCaseEvent = testCaseEvent;
        this.count = new AtomicInteger(initialCount);
    }

    public int increaseCount() {
        return count.incrementAndGet();
    }

    public TestCaseEvent getTestCaseEvent() {
        return testCaseEvent;
    }

    public int getCount() {
        return count.get();
    }

    public TestCaseEventCounter withIncreasedCount() {
        increaseCount();
        return this;
    }

    @Override
    public int hashCode() {
        return testCaseEvent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TestCaseEventCounter other = (TestCaseEventCounter) obj;
        return Objects.equal(this.testCaseEvent, other.testCaseEvent);
    }
}
