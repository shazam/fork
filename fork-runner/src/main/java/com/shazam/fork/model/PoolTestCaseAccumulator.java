package com.shazam.fork.model;

public interface PoolTestCaseAccumulator {
    void record(Pool pool, TestCaseEvent testCaseEvent);

    int getCount(Pool pool, TestCaseEvent testCaseEvent);

    int getCount(TestCaseEvent testCaseEvent);
}
