package com.shazam.fork.summary;

import com.shazam.fork.aggregator.AggregatedTestResult;
import org.junit.Test;

import static com.shazam.fork.aggregator.AggregatedTestResult.Builder.aggregatedTestResult;
import static com.shazam.fork.aggregator.PoolTestResult.Builder.poolTestResult;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OutcomeAggregatorTest {
    @Test
    public void returnsFalseWhenThereAreFatalCrashedTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withFatalCrashedTests(singletonList(aTestResult().build()))
                .build();

        boolean isSuccessful = new OutcomeAggregator().aggregate(aggregatedTestResult);

        assertThat(isSuccessful, equalTo(false));
    }

    @Test
    public void returnsFalseWhenThereAreFailedTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withPoolTestResults(asList(
                        poolTestResult()
                                .withPool(aDevicePool().build())
                                .withTestResults(asList(
                                        aTestResult()
                                                .withErrorTrace("aTrace")
                                                .build(),
                                        aTestResult().build()
                                ))
                                .build(),
                        poolTestResult()
                                .withPool(aDevicePool().build())
                                .withTestResults(singletonList(aTestResult().build()))
                                .build()
                ))
                .build();

        boolean isSuccessful = new OutcomeAggregator().aggregate(aggregatedTestResult);

        assertThat(isSuccessful, equalTo(false));
    }

    @Test
    public void returnsTrueWhenThereAreOnlyPassedAndIgnoredTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withPoolTestResults(asList(
                        poolTestResult()
                                .withPool(aDevicePool().build())
                                .withTestResults(singletonList(aTestResult().build()))
                                .build(),
                        poolTestResult()
                                .withPool(aDevicePool().build())
                                .withTestResults(asList(
                                        aTestResult()
                                                .withIgnored(true)
                                                .build(),
                                        aTestResult().build()
                                ))
                                .build()
                ))
                .build();

        boolean isSuccessful = new OutcomeAggregator().aggregate(aggregatedTestResult);

        assertThat(isSuccessful, equalTo(true));
    }
}