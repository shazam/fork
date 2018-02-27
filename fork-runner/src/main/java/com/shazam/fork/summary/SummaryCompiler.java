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

import com.google.common.collect.Sets;
import com.shazam.fork.ForkConfiguration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.runner.PoolTestRunner.DROPPED_BY;
import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.ResultStatus.ERROR;
import static com.shazam.fork.summary.ResultStatus.FAIL;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

public class SummaryCompiler {
    private final ForkConfiguration configuration;
    private final DeviceTestFilesRetriever deviceTestFilesRetriever;

    public SummaryCompiler(ForkConfiguration configuration, DeviceTestFilesRetriever deviceTestFilesRetriever) {
        this.configuration = configuration;
        this.deviceTestFilesRetriever = deviceTestFilesRetriever;
    }

    Summary compileSummary(Collection<Pool> pools, Collection<TestCaseEvent> testCases) {
        Summary.Builder summaryBuilder = aSummary();

        Set<TestResult> testResults = Sets.newHashSet();
        for (Pool pool : pools) {
            Collection<TestResult> testResultsForPool = getTestResultsForPool(pool);
            testResults.addAll(testResultsForPool);

            PoolSummary poolSummary = aPoolSummary()
                    .withPoolName(pool.getName())
                    .addTestResults(testResultsForPool)
                    .build();

            summaryBuilder.addPoolSummary(poolSummary);
            addFailedOrFatalCrashedTests(testResultsForPool, summaryBuilder);
        }

        Collection<TestResult> ignoredTestResults = getIgnoredTestResults(testCases);
        addIgnoredTests(ignoredTestResults, summaryBuilder);
        testResults.addAll(ignoredTestResults);

        Collection<TestResult> fatalCrashedTests = getFatalCrashedTests(testResults, testCases);
        addFatalCrashedTests(fatalCrashedTests, summaryBuilder);

        summaryBuilder.withTitle(configuration.getTitle());
        summaryBuilder.withSubtitle(configuration.getSubtitle());

        return summaryBuilder.build();
    }

    private Collection<TestResult> getTestResultsForPool(Pool pool) {
        Set<TestResult> testResults = Sets.newHashSet();

        Collection<TestResult> testResultsForPoolDevices = pool.getDevices()
                .stream()
                .map(device -> deviceTestFilesRetriever.getTestResultsForDevice(pool, device))
                .reduce(Sets.newHashSet(), (accum, set) -> {
                    accum.addAll(set);
                    return accum;
                });
        testResults.addAll(testResultsForPoolDevices);

        Device watchdog = getPoolWatchdog(pool.getName());
        Collection<TestResult> testResultsForWatchdog =
                deviceTestFilesRetriever.getTestResultsForDevice(pool, watchdog);
        testResults.addAll(testResultsForWatchdog);

        return testResults;
    }

    private static Device getPoolWatchdog(String poolName) {
        return aDevice()
                .withSerial(DROPPED_BY + poolName)
                .withManufacturer("Clumsy-" + poolName)
                .withModel("Clumsy=" + poolName)
                .build();
    }

    private static void addFailedOrFatalCrashedTests(Collection<TestResult> testResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : testResults) {
            int totalFailureCount = testResult.getTotalFailureCount();
            if (totalFailureCount > 0) {
                String failedTest = format(ENGLISH, "%d times %s", totalFailureCount, getTestResultData(testResult));
                summaryBuilder.addFailedTests(failedTest);
            } else if (testResult.getResultStatus() == ERROR || testResult.getResultStatus() == FAIL) {
                summaryBuilder.addFatalCrashedTest(getTestResultData(testResult));
            }
        }
    }

    private static Collection<TestResult> getIgnoredTestResults(Collection<TestCaseEvent> testCases) {
        return testCases.stream()
                .filter(TestCaseEvent::isIgnored)
                .map(testCaseEvent -> aTestResult()
                        .withTestClass(testCaseEvent.getTestClass())
                        .withTestMethod(testCaseEvent.getTestMethod())
                        .withIgnored(true)
                        .build())
                .collect(Collectors.toSet());
    }

    private static void addIgnoredTests(Collection<TestResult> ignoredTestResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : ignoredTestResults) {
            summaryBuilder.addIgnoredTest(testResult.getTestFullName());
        }
    }

    private static Collection<TestResult> getFatalCrashedTests(Collection<TestResult> processedTestResults,
                                                               Collection<TestCaseEvent> testCases) {
        Set<TestResultItem> processedTests = processedTestResults.stream()
                .map(testResult -> {
                    // Parameterized runner (and probably others) will generate methods names
                    // like testMethod[2] or testMethod[foo 2 bar]
                    // But they will be just 'testMethod' in allTests set
                    String testMethodWithChildren = testResult.getTestMethod();
                    String testMethod = testMethodWithChildren.split("\\[")[0];
                    return new TestResultItem(testResult.getTestClass(), testMethod);
                })
                .collect(Collectors.toSet());
        Set<TestResultItem> allTests = testCases.stream()
                .map(testCaseEvent -> new TestResultItem(testCaseEvent.getTestClass(), testCaseEvent.getTestMethod()))
                .collect(Collectors.toSet());

        return Sets.difference(allTests, processedTests)
                .stream()
                .map(TestResultItem::toTestResult)
                .collect(Collectors.toSet());
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

    private static class TestResultItem {
        private final String testClass;
        private final String testMethod;

        TestResultItem(String testClass, String testMethod) {
            this.testClass = testClass;
            this.testMethod = testMethod;
        }

        TestResult toTestResult() {
            return aTestResult()
                    .withTestClass(testClass)
                    .withTestMethod(testMethod)
                    .build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestResultItem that = (TestResultItem) o;
            return Objects.equals(testClass, that.testClass) &&
                    Objects.equals(testMethod, that.testMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(testClass, testMethod);
        }
    }
}
