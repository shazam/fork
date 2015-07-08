/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter.model;

public class Build {
    private final String buildId;
    private final String link;

    public String getBuildId() {
        return buildId;
    }

    public String getLink() {
        return link;
    }

    private Build(Builder builder) {
        this.buildId = builder.buildId;
        this.link = builder.link;
    }

    public static class Builder {
        private String buildId;
        private String link;

        public static Builder aBuild() {
            return new Builder();
        }

        public Builder withBuildId(String buildId) {
            this.buildId = buildId;
            return this;
        }

        public Builder withLink(String link) {
            this.link = link;
            return this;
        }

        public Build build() {
            return new Build(this);
        }
    }
}
