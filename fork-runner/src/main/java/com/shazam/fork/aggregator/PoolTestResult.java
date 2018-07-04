package com.shazam.fork.aggregator;

import com.shazam.fork.model.Pool;
import com.shazam.fork.summary.TestResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

public final class PoolTestResult {
    private final Pool pool;
    private final List<TestResult> testResults;

    private PoolTestResult(Builder builder) {
        this.pool = builder.pool;
        this.testResults = builder.testResults;
    }

    @Nonnull
    public Pool getPool() {
        return pool;
    }

    @Nonnull
    public List<TestResult> getTestResults() {
        return unmodifiableList(testResults);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PoolTestResult that = (PoolTestResult) o;
        return Objects.equals(pool, that.pool) &&
                Objects.equals(testResults, that.testResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pool, testResults);
    }

    public static class Builder {
        private Pool pool;
        private List<TestResult> testResults = new ArrayList<>();

        private Builder() {
        }

        public static Builder poolTestResult() {
            return new Builder();
        }

        public Builder withPool(Pool pool) {
            this.pool = pool;
            return this;
        }

        public Builder withTestResults(List<TestResult> testResults) {
            this.testResults.clear();
            this.testResults.addAll(testResults);
            return this;
        }

        public PoolTestResult build() {
            return new PoolTestResult(this);
        }
    }
}
