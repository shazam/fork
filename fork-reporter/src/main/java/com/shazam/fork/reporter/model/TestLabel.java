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

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

import static com.google.common.base.Objects.equal;

public class TestLabel {
    @Nonnull private final String className;
    @Nonnull private final String method;

    @Nonnull
    public String getClassName() {
        return className;
    }

    @Nonnull
    public String getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(className, method);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TestLabel)) {
            return false;
        }

        TestLabel otherTestLabel = (TestLabel) obj;

        return equal(className, otherTestLabel.getClassName()) &&
                equal(method, otherTestLabel.getMethod());
    }

    private TestLabel(Builder builder) {
        this.className = builder.className;
        this.method = builder.method;
    }

    public static class Builder {
        private String className = "";
        private String method = "";

        public static Builder testLabel() {
            return new Builder();
        }

        public Builder withClassName(@Nonnull String className) {
            this.className = className;
            return this;
        }

        public Builder withMethod(@Nonnull String method) {
            this.method = method;
            return this;
        }

        public TestLabel build() {
            return new TestLabel(this);
        }
    }
}
