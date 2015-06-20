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

import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.DEFAULT_STYLE;

/**
 * Holds test class and method information
 */
public class TestClass {
    private final String name;
	private final Collection<TestMethod> methods;

    private TestClass(Builder builder) {
        this.name = builder.name;
        this.methods = builder.methods;
    }

    public String getName() {
		return name;
	}

    @Override
    public String toString() {
        return reflectionToString(this, DEFAULT_STYLE);
    }

    public Collection<TestMethod> getMethods() {
        return methods;
    }

    public Collection<TestMethod> getUnignoredMethods() {
        Collection<TestMethod> unignoredMethods = new ArrayList<>();
		for (TestMethod method : methods) {
			if (!method.isIgnored()) {
				unignoredMethods.add(method);
			}
		}
		return unignoredMethods;
	}

	public Collection<TestMethod> getIgnoredMethods() {
        Collection<TestMethod> ignoredMethods = new ArrayList<>();
		for (TestMethod method : methods) {
			if (method.isIgnored()) {
				ignoredMethods.add(method);
			}
		}
		return ignoredMethods;
	}

    public static class Builder {
        private String name;
        private final Collection<TestMethod> methods = new ArrayList<>();

        public static Builder testClass() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withMethods(Collection<TestMethod> testMethods) {
            methods.clear();
            methods.addAll(testMethods);
            return this;
        }

        public TestClass build() {
            return new TestClass(this);
        }
    }
}
