package com.shazam.fork.summary;

import org.junit.Test;

import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OutcomeAggregatorTest {
    @Test
    public void returnsFalseIfThereAreSkippedTests() {
        Summary summary = aSummary()
                .addSkippedTest("com.example.SkippedTest:testMethod")
                .addPoolSummary(aPoolSummary()
                        .withPoolName("pool")
                        .addTestResults(singleton(aTestResult()
                                .withTestClass("com.example.SuccessfulTest")
                                .withTestMethod("testMethod")
                                .withTimeTaken(15.0f)
                                .build()))
                        .build())
                .build();

        boolean successful = new OutcomeAggregator().aggregate(summary);

        assertThat(successful, equalTo(false));
    }

    @Test
    public void returnsTrueIfThereAreOnlyPassedAndIgnoredTests() {
        Summary summary = aSummary()
                .addIgnoredTest("com.example.IgnoredTest:testMethod")
                .addPoolSummary(aPoolSummary()
                        .withPoolName("pool")
                        .addTestResults(asList(
                                aTestResult()
                                        .withTestClass("com.example.SuccessfulTest")
                                        .withTestMethod("testMethod")
                                        .withTimeTaken(15.0f)
                                        .build(),
                                aTestResult()
                                        .withTestClass("com.example.IgnoredTest")
                                        .withTestMethod("testMethod")
                                        .withIgnored(true)
                                        .build()
                        ))
                        .build())
                .build();

        boolean successful = new OutcomeAggregator().aggregate(summary);

        assertThat(successful, equalTo(true));
    }
}