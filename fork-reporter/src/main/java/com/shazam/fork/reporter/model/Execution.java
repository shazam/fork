/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter.model;

import com.shazam.fork.summary.Summary;

public class Execution {

    private final String buildId;
    private final Summary summary;

    private Execution(Builder builder) {
        this.summary = builder.summary;
        this.buildId = builder.buildId;
    }

    public String getBuildId() {
        return buildId;
    }

    public Summary getSummary() {
        return summary;
    }

    public static class Builder {
        private Summary summary;
        private String buildId;

        public static Builder execution() {
            return new Builder();
        }

        public Builder withBuildId(String buildId) {
            this.buildId = buildId;
            return this;
        }

        public Builder withSummary(Summary summary) {
            this.summary = summary;
            return this;
        }

        public Execution build() {
            return new Execution(this);
        }
    }
}
