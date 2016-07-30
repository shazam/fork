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
package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import java.util.Queue;

import javax.annotation.Nullable;

public class TestRunParameters {
	private final Queue<TestCaseEvent> testQueue;
	private final String testPackage;
	private final String testRunner;
	private final boolean isCoverageEnabled;
	private final IRemoteAndroidTestRunner.TestSize testSize;
	private final int testOutputTimeout;
	private final Device device;
    private final String excludedAnnotation;
	private final Pool pool;

	public Queue<TestCaseEvent> getTestQueue() {
		return testQueue;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public String getTestRunner() {
		return testRunner;
	}

	@Nullable
	public IRemoteAndroidTestRunner.TestSize getTestSize() {
		return testSize;
	}

	public int getTestOutputTimeout() {
		return testOutputTimeout;
	}

	public Device getDevice() {
		return device;
	}

	public boolean isCoverageEnabled(){
		return isCoverageEnabled;
	}

	public String getExcludedAnnotation() {
		return excludedAnnotation;
	}

	public Pool getPool() {
		return pool;
	}

	public static class Builder {
		private Queue<TestCaseEvent> testQueue;
		private String testPackage;
		private String testRunner;
		private boolean isCoverageEnabled;
		private IRemoteAndroidTestRunner.TestSize testSize;
		private Device device;
		private int testOutputTimeout;
		private String excludedAnnotation;
		private Pool pool;

		public static Builder testRunParameters() {
			return new Builder();
		}

		public Builder withTestQueue(Queue<TestCaseEvent> testQueue) {
			this.testQueue = testQueue;
			return this;
		}

		public Builder withTestPackage(String testPackage) {
			this.testPackage = testPackage;
			return this;
		}

		public Builder withTestRunner(String testRunner) {
			this.testRunner = testRunner;
			return this;
		}

		public Builder withTestSize(IRemoteAndroidTestRunner.TestSize testSize) {
			this.testSize = testSize;
			return this;
		}

		public Builder withTestOutputTimeout(int testOutputTimeout) {
			this.testOutputTimeout = testOutputTimeout;
			return this;
		}

		public Builder withDevice(Device device) {
			this.device = device;
			return this;
		}

		public Builder withCoverageEnabled(boolean isCoverageEnabled){
			this.isCoverageEnabled = isCoverageEnabled;
			return this;
		}

		public Builder withExcludedAnnotation(String excludedAnnotation) {
			this.excludedAnnotation = excludedAnnotation;
			return this;
		}

		public Builder withPool(Pool pool) {
			this.pool = pool;
			return this;
		}

		public TestRunParameters build() {
			return new TestRunParameters(this);
		}
	}

	private TestRunParameters(Builder builder) {
		testQueue = builder.testQueue;
		testPackage = builder.testPackage;
		testRunner = builder.testRunner;
		testSize = builder.testSize;
		testOutputTimeout = builder.testOutputTimeout;
		device = builder.device;
		isCoverageEnabled = builder.isCoverageEnabled;
		this.excludedAnnotation = builder.excludedAnnotation;
		pool = builder.pool;
	}
}
