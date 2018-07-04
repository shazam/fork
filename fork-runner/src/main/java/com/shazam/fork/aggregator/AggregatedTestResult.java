package com.shazam.fork.aggregator;

import com.shazam.fork.summary.TestResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

public final class AggregatedTestResult {
    private final List<PoolTestResult> poolTestResults;
    private final List<TestResult> ignoredTests;
    private final List<TestResult> fatalCrashedTests;

    private AggregatedTestResult(Builder builder) {
        this.poolTestResults = builder.poolTestResults;
        this.ignoredTests = builder.ignoredTests;
        this.fatalCrashedTests = builder.fatalCrashedTests;
    }

    @Nonnull
    public List<PoolTestResult> getPoolTestResults() {
        return unmodifiableList(poolTestResults);
    }

    @Nonnull
    public List<TestResult> getIgnoredTests() {
        return unmodifiableList(ignoredTests);
    }

    @Nonnull
    public List<TestResult> getFatalCrashedTests() {
        return unmodifiableList(fatalCrashedTests);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedTestResult that = (AggregatedTestResult) o;
        return Objects.equals(poolTestResults, that.poolTestResults) &&
                Objects.equals(ignoredTests, that.ignoredTests) &&
                Objects.equals(fatalCrashedTests, that.fatalCrashedTests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poolTestResults, ignoredTests, fatalCrashedTests);
    }

    public static class Builder {
        private List<PoolTestResult> poolTestResults = new ArrayList<>();
        private List<TestResult> ignoredTests = new ArrayList<>();
        private List<TestResult> fatalCrashedTests = new ArrayList<>();

        private Builder() {
        }

        public static Builder aggregatedTestResult() {
            return new Builder();
        }

        public Builder withPoolTestResults(List<PoolTestResult> poolTestResults) {
            this.poolTestResults.clear();
            this.poolTestResults.addAll(poolTestResults);
            return this;
        }

        public Builder withIgnoredTests(List<TestResult> ignoredTests) {
            this.ignoredTests.clear();
            this.ignoredTests.addAll(ignoredTests);
            return this;
        }

        public Builder withFatalCrashedTests(List<TestResult> fatalCrashedTests) {
            this.fatalCrashedTests.clear();
            this.fatalCrashedTests.addAll(fatalCrashedTests);
            return this;
        }

        public AggregatedTestResult build() {
            return new AggregatedTestResult(this);
        }
    }
}
