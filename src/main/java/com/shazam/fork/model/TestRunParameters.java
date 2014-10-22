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

import com.android.ddmlib.IDevice;

public class TestRunParameters {
	private final TestClass test;
	private final String testPackage;
	private final String testRunner;
	private final IDevice deviceInterface;

	public TestClass getTest() {
		return test;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public String getTestRunner() {
		return testRunner;
	}

	public IDevice getDeviceInterface() {
		return deviceInterface;
	}

	public static class Builder {
		private TestClass test;
		private String testPackage;
		private String testRunner;
		private IDevice deviceInterface;

		public static Builder testRunParameters() {
			return new Builder();
		}

		public Builder withTest(TestClass test) {
			this.test = test;
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

		public Builder withDeviceInterface(IDevice deviceInterface) {
			this.deviceInterface = deviceInterface;
			return this;
		}

		public TestRunParameters build() {
			return new TestRunParameters(this);
		}
	}

	private TestRunParameters(Builder builder) {
		test = builder.test;
		testPackage = builder.testPackage;
		testRunner = builder.testRunner;
		deviceInterface = builder.deviceInterface;
	}
}
