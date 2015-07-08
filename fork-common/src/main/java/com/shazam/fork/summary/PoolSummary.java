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
package com.shazam.fork.summary;

import java.util.ArrayList;
import java.util.Collection;

public class PoolSummary {
	private final String poolName;
	private final Collection<TestResult> testResults;

	public String getPoolName() {
		return poolName;
	}

	public Collection<TestResult> getTestResults() {
		return testResults;
	}

	public static class Builder {
		private String poolName;
		private final Collection<TestResult> testResults = new ArrayList<>();

		public static Builder aPoolSummary() {
			return new Builder();
		}

		public Builder withPoolName(String poolName) {
			this.poolName = poolName;
			return this;
		}

		public Builder addTestResults(Collection<TestResult> testResults) {
			this.testResults.addAll(testResults);
			return this;
		}

		public PoolSummary build() {
			return new PoolSummary(this);
		}
	}

	private PoolSummary(Builder builder) {
		testResults = builder.testResults;
		poolName = builder.poolName;
	}
}
