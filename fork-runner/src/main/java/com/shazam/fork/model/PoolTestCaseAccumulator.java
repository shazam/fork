package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;

public interface PoolTestCaseAccumulator {
    void record(Pool pool, TestCaseEvent testCaseEvent);

    int getCount(Pool pool, TestIdentifier testIdentifier);

    int getCount(TestIdentifier testIdentifier);
}
