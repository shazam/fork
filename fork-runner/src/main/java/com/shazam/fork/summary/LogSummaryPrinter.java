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

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.shazam.fork.summary.ResultStatus.ERROR;
import static com.shazam.fork.summary.ResultStatus.FAIL;
import static com.shazam.fork.summary.ResultStatus.PASS;
import static java.lang.String.format;

public class LogSummaryPrinter implements SummaryPrinter {

    private static final Logger logger = LoggerFactory.getLogger(LogSummaryPrinter.class);

    @Override
    public void print(boolean isSuccessful, Summary summary) {
        for (ResultStatus resultStatus : new ResultStatus[]{FAIL, ERROR}) {
            for (PoolSummary poolSummary : summary.getPoolSummaries()) {
                StringBuilder out = getPoolSummary(poolSummary, resultStatus);
                if (out.length() != 0) {
                    logger.info(out.toString());
                }
            }
        }
        for (PoolSummary poolSummary : summary.getPoolSummaries()) {
            printMiniSummary(poolSummary);
        }
        List<String> suppressedTests = summary.getIgnoredTests();
        if (suppressedTests.isEmpty()) {
            logger.info("No suppressed tests.");
        } else {
            logger.info("Suppressed tests:");
            for (String s : suppressedTests) {
                logger.info(s);
            }
        }
    }

    private void printMiniSummary(PoolSummary poolSummary) {
        logger.info(format("% 3d E  % 3d F  % 3d P: %s",
                getResultsWithStatus(poolSummary.getTestResults(), ERROR).size(),
                getResultsWithStatus(poolSummary.getTestResults(), FAIL).size(),
                getResultsWithStatus(poolSummary.getTestResults(), PASS).size(),
                poolSummary.getPoolName()
        ));
    }

    private StringBuilder getPoolSummary(PoolSummary poolSummary, ResultStatus resultStatus) {
        StringBuilder summary = printTestsWithStatus(poolSummary, resultStatus);
        if (summary.length() > 0) {
            final String poolName = poolSummary.getPoolName();
            summary.insert(0, format("%s Results for device pool: %s\n", resultStatus, poolName));
            summary.insert(0, "____________________________________________________________________________________\n");
        }
        return summary;
    }

    private StringBuilder printTestsWithStatus(PoolSummary poolSummary, ResultStatus status) {
        StringBuilder summary = new StringBuilder();
        final Collection<TestResult> resultsWithStatus = getResultsWithStatus(poolSummary.getTestResults(), status);
        if (!resultsWithStatus.isEmpty()) {
            for (TestResult testResult : resultsWithStatus) {
                summary.append(format("%s %s#%s on %s %s\n",
                        testResult.getResultStatus(),
                        testResult.getTestClass(),
                        testResult.getTestMethod(),
                        testResult.getDevice().getManufacturer(),
                        testResult.getDevice().getModelName()));
            }
        }
        return summary;
    }

    private Collection<TestResult> getResultsWithStatus(Collection<TestResult> testResults, final ResultStatus resultStatus) {
        return filter(testResults, new Predicate<TestResult>() {
            @Override
            public boolean apply(@Nullable TestResult testResult) {
                return testResult.getResultStatus().equals(resultStatus);
            }
        });
    }
}
