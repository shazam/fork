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

import com.shazam.fork.ForkConfiguration;
import com.shazam.fork.aggregator.AggregatedTestResult;
import com.shazam.fork.aggregator.PoolTestResult;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.ResultStatus.ERROR;
import static com.shazam.fork.summary.ResultStatus.FAIL;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

public class SummaryCompiler {
    private final ForkConfiguration configuration;

    public SummaryCompiler(ForkConfiguration configuration) {
        this.configuration = configuration;
    }

    @Nonnull
    Summary compileSummary(@Nonnull AggregatedTestResult aggregatedTestResult) {
        Summary.Builder summaryBuilder = aSummary();

        for (PoolTestResult poolTestResult : aggregatedTestResult.getPoolTestResults()) {
            List<TestResult> testResults = poolTestResult.getTestResults();
            PoolSummary poolSummary = aPoolSummary()
                    .withPoolName(poolTestResult.getPool().getName())
                    .addTestResults(testResults)
                    .build();

            summaryBuilder.addPoolSummary(poolSummary);
            addFailedTests(testResults, summaryBuilder);
        }

        addIgnoredTests(aggregatedTestResult.getIgnoredTests(), summaryBuilder);
        addFatalCrashedTests(aggregatedTestResult.getFatalCrashedTests(), summaryBuilder);

        summaryBuilder.withTitle(configuration.getTitle());
        summaryBuilder.withSubtitle(configuration.getSubtitle());

        return summaryBuilder.build();
    }

    private static void addFailedTests(Collection<TestResult> testResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : testResults) {
            int totalFailureCount = testResult.getTotalFailureCount();
            ResultStatus resultStatus = testResult.getResultStatus();
            if (totalFailureCount > 0 || resultStatus == ERROR || resultStatus == FAIL) {
                summaryBuilder.addFailedTests(getFailedTestMessage(testResult));
            }
        }
    }

    private static String getFailedTestMessage(TestResult testResult) {
        int totalFailureCount = testResult.getTotalFailureCount();
        if (totalFailureCount > 0) {
            return format(ENGLISH, "%d times %s", totalFailureCount, getTestResultData(testResult));
        } else {
            return getTestResultData(testResult);
        }
    }

    private static void addIgnoredTests(Collection<TestResult> ignoredTestResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : ignoredTestResults) {
            summaryBuilder.addIgnoredTest(testResult.getTestFullName());
        }
    }

    private static void addFatalCrashedTests(Collection<TestResult> fatalCrashedTests, Summary.Builder summaryBuilder) {
        for (TestResult fatalCrashedTest : fatalCrashedTests) {
            summaryBuilder.addFatalCrashedTest(getTestResultData(fatalCrashedTest));
        }
    }

    private static String getTestResultData(TestResult testResult) {
        return format(ENGLISH, "%s#%s on %s", testResult.getTestClass(), testResult.getTestMethod(),
                testResult.getDeviceSerial());
    }
}
