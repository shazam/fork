package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.PoolTestCaseAccumulator;
import com.shazam.fork.model.TestCaseEvent;

public class FakePoolTestCaseAccumulator implements PoolTestCaseAccumulator {

    private int count = 0;

    public static FakePoolTestCaseAccumulator aFakePoolTestCaseAccumulator(){
        return new FakePoolTestCaseAccumulator();
    }

    public FakePoolTestCaseAccumulator thatAlwaysReturns(int count){
        this.count = count;
        return this;
    }

    @Override
    public void record(Pool pool, TestCaseEvent testCaseEvent) {
    }

    @Override
    public int getCount(Pool pool, TestCaseEvent testCaseEvent) {
        return count;
    }

    @Override
    public int getCount(TestCaseEvent testCaseEvent) {
        return count;
    }
}
