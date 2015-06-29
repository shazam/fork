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

import com.shazam.fork.summary.ResultStatus;

public class TestInstance {
    private final ResultStatus resultStatus;
    private final String link;

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public String getLink() {
        return link;
    }

    private TestInstance(Builder builder) {
        this.resultStatus = builder.resultStatus;
        this.link = builder.link;
    }

    public static class Builder {
        private ResultStatus resultStatus;
        private String link;

        public static Builder testInstance() {
            return new Builder();
        }

        public Builder withResultStatus(ResultStatus resultStatus) {
            this.resultStatus = resultStatus;
            return this;
        }

        public Builder withLink(String link) {
            this.link = link;
            return this;
        }

        public TestInstance build() {
            return new TestInstance(this);
        }
    }
}
