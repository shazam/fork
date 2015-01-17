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

import com.google.common.base.Function;

import org.apache.commons.lang3.BooleanUtils;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.summary.TestResult.ResultStatus.PASS;

public class OutcomeAggregator {

	public boolean aggregate(Summary summary) {
		final List<PoolSummary> poolSummaries = summary.getPoolSummaries();
		Collection<Boolean> poolOutcomes = transform(poolSummaries, toPoolOutcome());
		return and(poolOutcomes);
	}

	public static Function<? super PoolSummary, Boolean> toPoolOutcome() {
		return new Function<PoolSummary, Boolean>() {
			@Override
			@Nullable
			public Boolean apply(@Nullable PoolSummary input) {
				final Collection<TestResult> testResults = input.getTestResults();
				final Collection<Boolean> testOutcomes = transform(testResults, toResultOutcome());
				return and(testOutcomes);
			}
		};
	}

	private static Function<TestResult, Boolean> toResultOutcome() {
		return new Function<TestResult, Boolean>() {
			@Override
			@Nullable
			public Boolean apply(@Nullable TestResult input) {
				return PASS.equals(input.getResultStatus());
			}
		};
	}

    private static Boolean and(final Collection<Boolean> booleans) {
		return BooleanUtils.and(booleans.toArray(new Boolean[booleans.size()]));
	}

}
