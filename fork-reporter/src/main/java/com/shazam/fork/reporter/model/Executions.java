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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Executions {
    private final List<Execution> executions;

    private Executions(Builder builder) {
        this.executions = builder.executions;
    }

    @Nonnull
    public List<Execution> getExecutions() {
        return executions;
    }

    public static class Builder {
        private List<Execution> executions = new ArrayList<>();

        public static Builder executions() {
            return new Builder();
        }

        public Builder withExecutions(@Nonnull List<Execution> executions) {
            this.executions = executions;
            return this;
        }

        public Executions build() {
            return new Executions(this);
        }
    }
}
