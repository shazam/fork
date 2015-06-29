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

import com.google.common.collect.Table;

public class PoolHistory {
    private final String name;
    private final String readableName;
    private final Table<TestLabel, Build, TestInstance> historyTable;

    public String getName() {
        return name;
    }

    public String getReadableName() {
        return readableName;
    }

    public Table<TestLabel, Build, TestInstance> getHistoryTable() {
        return historyTable;
    }

    private PoolHistory(Builder builder) {
        this.name = builder.name;
        this.readableName = builder.readableName;
        this.historyTable = builder.historyTable;
    }

    public static class Builder {
        private String name;
        private String readableName;
        private Table<TestLabel, Build, TestInstance> historyTable;

        public static Builder poolHistory() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withReadableName(String readableName) {
            this.readableName = readableName;
            return this;
        }

        public Builder withHistoryTable(Table<TestLabel, Build, TestInstance> historyTable) {
            this.historyTable = historyTable;
            return this;
        }

        public PoolHistory build() {
            return new PoolHistory(this);
        }
    }
}