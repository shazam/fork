package com.shazam.fork.aggregator;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface Aggregator {
    @Nonnull
    AggregatedTestResult aggregateTestResults(@Nonnull Collection<Pool> pools,
                                              @Nonnull Collection<TestCaseEvent> testCases);
}
