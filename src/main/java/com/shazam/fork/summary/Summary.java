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
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;


public class Summary {
	private final List<PoolSummary> poolSummaries;
	private final String title;
	private final String subtitle;
	private final ArrayList<String> suppressedTests;

	public List<PoolSummary> getPoolSummaries() {
		return poolSummaries;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public ArrayList<String> getSuppressedTests() {
		return suppressedTests;
	}

	public static class Builder {
		private final List<PoolSummary> poolSummaries = new ArrayList<>();
        private final ArrayList<String> suppressedTests = new ArrayList<>();
        private String title = "Report Title";
        private String subtitle = "Report Subtitle";

		public static Builder aSummary() {
			return new Builder();
		}

		public Builder addPoolSummary(PoolSummary poolSummary) {
			poolSummaries.add(poolSummary);
			return this;
		}

		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder withSubtitle(String subtitle) {
			this.subtitle = subtitle;
			return this;
		}

		public Builder addSuppressedTest(String s) {
			this.suppressedTests.add(s);
			return this;
		}

		public Summary build() {
			return new Summary(this);
		}
	}

	@Override
	public String toString() {
		return reflectionToString(this, MULTI_LINE_STYLE);
	}

	private Summary(Builder builder) {
		poolSummaries = builder.poolSummaries;
		title = builder.title;
		subtitle = builder.subtitle;
		suppressedTests = builder.suppressedTests;
	}
}
