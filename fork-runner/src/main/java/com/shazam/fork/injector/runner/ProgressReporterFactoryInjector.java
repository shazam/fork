package com.shazam.fork.injector.runner;

import com.shazam.fork.runner.ProgressReporterFactory;

import javax.annotation.Nonnull;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;
import static com.shazam.fork.injector.accumulator.PoolTestCaseFailureAccumulatorInjector.poolTestCaseFailureAccumulator;
import static com.shazam.fork.injector.runner.PoolProgressTrackersInjector.poolProgressTrackers;

public final class ProgressReporterFactoryInjector {
    private ProgressReporterFactoryInjector() {
        throw new AssertionError("No instances");
    }

    @Nonnull
    public static ProgressReporterFactory progressReporterFactory() {
        return new ProgressReporterFactory(
                configuration().getTotalAllowedRetryQuota(),
                configuration().getRetryPerTestCaseQuota(),
                poolProgressTrackers(),
                poolTestCaseFailureAccumulator()
        );
    }
}
