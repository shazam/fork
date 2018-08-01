package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.PoolTestCaseAccumulator;
import com.shazam.fork.model.TestCaseEvent;

import javax.annotation.Nonnull;

public class FakePoolTestCaseAccumulator implements PoolTestCaseAccumulator {
    private int poolCount;
    private int testCaseCount;

    private FakePoolTestCaseAccumulator() {
    }

    @Nonnull
    public static FakePoolTestCaseAccumulator fakePoolTestCaseAccumulator() {
        return new FakePoolTestCaseAccumulator();
    }

    @Nonnull
    public FakePoolTestCaseAccumulator thatAlwaysReturnsPoolCount(int count) {
        poolCount = count;
        return this;
    }

    @Nonnull
    public FakePoolTestCaseAccumulator thatAlwaysReturnsTestCaseCount(int count) {
        testCaseCount = count;
        return this;
    }

    @Override
    public void record(Pool pool, TestCaseEvent testCaseEvent) {
    }

    @Override
    public int getCount(Pool pool, TestCaseEvent testCaseEvent) {
        return poolCount;
    }

    @Override
    public int getCount(TestCaseEvent testCaseEvent) {
        return testCaseCount;
    }
}
