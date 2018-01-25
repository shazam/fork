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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.summary.ResultStatus.IGNORED;
import static com.shazam.fork.summary.ResultStatus.PASS;

public class OutcomeAggregator {
    private static final Logger logger = LoggerFactory.getLogger(OutcomeAggregator.class);

    public boolean aggregate(Summary summary) {
        if (summary == null || summary.getPoolSummaries().isEmpty() || !summary.getFatalCrashedTests().isEmpty()) {
            if (summary != null && !summary.getFatalCrashedTests().isEmpty()) {
                logger.error("There are tests left unprocessed: " + summary.getFatalCrashedTests());
            }
            return false;
        }

        List<PoolSummary> poolSummaries = summary.getPoolSummaries();
        Collection<Boolean> poolOutcomes = transform(poolSummaries, toPoolOutcome());
        return and(poolOutcomes);
    }

    public static Function<? super PoolSummary, Boolean> toPoolOutcome() {
        return new Function<PoolSummary, Boolean>() {
            @Override
            @Nullable
            public Boolean apply(@Nullable PoolSummary input) {
                final Collection<TestResult> testResults = input.getTestResults();
                final Collection<Boolean> testOutcomes = transform(testResults, toTestOutcome());
                return !testOutcomes.isEmpty() && and(testOutcomes);
            }
        };
    }

    private static Function<TestResult, Boolean> toTestOutcome() {
        return new Function<TestResult, Boolean>() {
            @Override
            @Nullable
            public Boolean apply(@Nullable TestResult input) {
                return PASS.equals(input.getResultStatus()) || IGNORED.equals(input.getResultStatus());
            }
        };
    }

    private static Boolean and(final Collection<Boolean> booleans) {
        return BooleanUtils.and(booleans.toArray(new Boolean[booleans.size()]));
    }
}
