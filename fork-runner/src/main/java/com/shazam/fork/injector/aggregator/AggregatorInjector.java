package com.shazam.fork.injector.aggregator;

import com.shazam.fork.aggregator.Aggregator;
import com.shazam.fork.aggregator.FilesRetrieverBasedAggregator;

import javax.annotation.Nonnull;

import static com.shazam.fork.injector.summary.DeviceTestFilesRetrieverInjector.deviceTestFilesRetriever;

public final class AggregatorInjector {
    private AggregatorInjector() {
        throw new AssertionError("No instances");
    }

    @Nonnull
    public static Aggregator aggregator() {
        return new FilesRetrieverBasedAggregator(deviceTestFilesRetriever());
    }
}
