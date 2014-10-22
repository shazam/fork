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
import java.util.List;

/**
 * Holds test class and method information
 */
public class TestClass {
    private final String testClassName;
	private final List<TestMethod> methods = new ArrayList<>();

	public TestClass(String testClassName) {
		this.testClassName = testClassName;
	}

    public String getClassName() {
		return testClassName;
	}
	public void addMethod(String methodString) {
		this.methods.add(new TestMethod(methodString));
	}

    public List<TestMethod> getUnsuppressedMethods() {
		List<TestMethod> unsuppressedMethods = new ArrayList<>();
		for (TestMethod method : methods) {
			if (!method.isSuppressed()) {
				unsuppressedMethods.add(method);
			}
		}
		return unsuppressedMethods;
	}

	public List<TestMethod> getSuppressedMethods() {
		List<TestMethod> suppressedMethods = new ArrayList<>();
		for (TestMethod method : methods) {
			if (method.isSuppressed()) {
				suppressedMethods.add(method);
			}
		}
		return suppressedMethods;
	}

	public TestMethod getMethod(String methodName) {
		for (TestMethod method : methods) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
