package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class FakeProgressReporterTrackers extends AbstractMap<Pool, PoolProgressTracker> implements Map<Pool, PoolProgressTracker> {

    private PoolProgressTracker poolProgressTracker;

    public static FakeProgressReporterTrackers aFakeProgressReporterTrackers() {
        return new FakeProgressReporterTrackers();
    }

    public FakeProgressReporterTrackers thatAlwaysReturns(PoolProgressTracker poolProgressTracker) {
        this.poolProgressTracker = poolProgressTracker;
        return this;
    }

    @Override
    public Set<Entry<Pool, PoolProgressTracker>> entrySet() {
        return null;
    }

    @Override
    public PoolProgressTracker get(Object key) {
        return poolProgressTracker;
    }

    @Override
    public boolean containsKey(Object key) {
        return true;
    }
}
