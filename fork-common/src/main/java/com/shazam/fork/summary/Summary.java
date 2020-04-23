/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.summary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;


public class Summary {
    private final List<PoolSummary> poolSummaries;
    private final String title;
    private final String subtitle;
    private final List<String> ignoredTests;
    private final List<String> failedTests;
    private final List<String> fatalCrashedTests;

    @Nonnull
    public List<PoolSummary> getPoolSummaries() {
        return unmodifiableList(poolSummaries);
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    @Nonnull
    public List<String> getIgnoredTests() {
        return unmodifiableList(ignoredTests);
    }

    @Nonnull
    public List<String> getFailedTests() {
        return unmodifiableList(failedTests);
    }

    @Nonnull
    public List<String> getFatalCrashedTests() {
        return unmodifiableList(fatalCrashedTests);
    }

    public static class Builder {
        private final List<PoolSummary> poolSummaries = new ArrayList<>();
        private final List<String> ignoredTests = new ArrayList<>();
        private String title = "Report Title";
        private String subtitle = "Report Subtitle";
        private List<String> failedTests = new ArrayList<>();
        private List<String> fatalCrashedTests = new ArrayList<>();

        public static Builder aSummary() {
            return new Builder();
        }

        public Builder addPoolSummary(PoolSummary poolSummary) {
            poolSummaries.add(poolSummary);
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder addIgnoredTest(String s) {
            this.ignoredTests.add(s);
            return this;
        }

        public Builder addFailedTests(String failedTests) {
            this.failedTests.add(failedTests);
            return this;
        }

        public Builder addFatalCrashedTest(String fatalCrashedTest) {
            fatalCrashedTests.add(fatalCrashedTest);
            return this;
        }

        public Summary build() {
            return new Summary(this);
        }
    }

    private Summary(Builder builder) {
        poolSummaries = builder.poolSummaries;
        title = builder.title;
        subtitle = builder.subtitle;
        ignoredTests = builder.ignoredTests;
        failedTests = builder.failedTests;
        fatalCrashedTests = builder.fatalCrashedTests;
    }
}
