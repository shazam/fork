/*
 * Copyright 2014 Shazam Entertainment Limited
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
package com.shazam.fork.model;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestMethod {
    private final String name;
    private final boolean ignored;

    private TestMethod(Builder builder) {
        this.name = builder.name;
        this.ignored = builder.ignored;
    }

    public String getName() {
        return name;
    }

    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public String toString() {
        return reflectionToString(this, SIMPLE_STYLE);
    }

    public static class Builder {
        private String name;
        private boolean ignored = false;

        public static Builder testMethod() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder willBeIgnored() {
            this.ignored = true;
            return this;
        }

        public TestMethod build() {
            return new TestMethod(this);
        }
    }
}
