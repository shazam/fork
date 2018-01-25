package com.shazam.fork.summary;

import org.junit.Test;

import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OutcomeAggregatorTest {
    @Test
    public void returnsFalseIfThereSkippedTests() {
        Summary summary = aSummary()
                .addSkippedTest("com.example.SkippedTest:testMethod")
                .addPoolSummary(aPoolSummary()
                        .withPoolName("pool")
                        .addTestResults(singleton(TestResult.Builder.aTestResult()
                                .withTestClass("com.example.SuccessfulTest")
                                .withTestMethod("testMethod")
                                .withTimeTaken(15.0f)
                                .build()))
                        .build())
                .build();

        boolean isAggregated = new OutcomeAggregator().aggregate(summary);

        assertThat(isAggregated, equalTo(false));
    }
}