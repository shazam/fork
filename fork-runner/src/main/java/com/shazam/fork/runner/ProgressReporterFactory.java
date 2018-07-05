package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.PoolTestCaseAccumulator;

import javax.annotation.Nonnull;
import java.util.Map;

public final class ProgressReporterFactory {
    private int totalAllowedRetryQuota;
    private int retryPerTestCaseQuota;
    private Map<Pool, PoolProgressTracker> poolProgressTrackers;
    private PoolTestCaseAccumulator failedTestCasesAccumulator;

    public ProgressReporterFactory(int totalAllowedRetryQuota,
                                   int retryPerTestCaseQuota,
                                   Map<Pool, PoolProgressTracker> poolProgressTrackers,
                                   PoolTestCaseAccumulator failedTestCasesAccumulator) {
        this.totalAllowedRetryQuota = totalAllowedRetryQuota;
        this.retryPerTestCaseQuota = retryPerTestCaseQuota;
        this.poolProgressTrackers = poolProgressTrackers;
        this.failedTestCasesAccumulator = failedTestCasesAccumulator;
    }

    @Nonnull
    public ProgressReporter createProgressReporter() {
        return new OverallProgressReporter(
                totalAllowedRetryQuota,
                retryPerTestCaseQuota,
                poolProgressTrackers,
                failedTestCasesAccumulator
        );
    }
}
