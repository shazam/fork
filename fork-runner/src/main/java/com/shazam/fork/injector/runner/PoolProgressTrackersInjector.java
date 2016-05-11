package com.shazam.fork.injector.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.runner.PoolProgressTracker;

import java.util.Map;

import static com.beust.jcommander.internal.Maps.newHashMap;

public class PoolProgressTrackersInjector {

    private PoolProgressTrackersInjector() {}

    public static Map<Pool, PoolProgressTracker> poolProgressTrackers() {
        return newHashMap();
    }
}
