package com.shazam.fork.aggregator;

import com.google.common.collect.Sets;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.summary.DeviceTestFilesRetriever;
import com.shazam.fork.summary.TestResult;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.shazam.fork.aggregator.AggregatedTestResult.Builder.aggregatedTestResult;
import static com.shazam.fork.aggregator.PoolTestResult.Builder.poolTestResult;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.runner.PoolTestRunner.DROPPED_BY;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public final class FilesRetrieverBasedAggregator implements Aggregator {
    private final DeviceTestFilesRetriever deviceTestFilesRetriever;

    public FilesRetrieverBasedAggregator(DeviceTestFilesRetriever deviceTestFilesRetriever) {
        this.deviceTestFilesRetriever = deviceTestFilesRetriever;
    }

    @Nonnull
    @Override
    public AggregatedTestResult aggregateTestResults(@Nonnull Collection<Pool> pools,
                                                     @Nonnull Collection<TestCaseEvent> testCases) {
        List<PoolTestResult> poolTestResults = pools.stream()
                .map(pool -> poolTestResult()
                        .withPool(pool)
                        .withTestResults(getTestResultsForPool(pool))
                        .build())
                .collect(toList());
        List<TestResult> ignoredTestResults = getIgnoredTestResults(testCases);

        List<TestResult> fatalCrashedTests =
                getFatalCrashedTests(getExecutedTests(poolTestResults, ignoredTestResults), testCases);

        return aggregatedTestResult()
                .withPoolTestResults(poolTestResults)
                .withIgnoredTests(ignoredTestResults)
                .withFatalCrashedTests(fatalCrashedTests)
                .build();
    }

    private List<TestResult> getTestResultsForPool(Pool pool) {
        List<TestResult> testResults = pool.getDevices()
                .stream()
                .map(device -> deviceTestFilesRetriever.getTestResultsForDevice(pool, device))
                .flatMap(Collection::stream)
                .collect(toList());

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

    private static List<TestResult> getIgnoredTestResults(Collection<TestCaseEvent> testCases) {
        return testCases.stream()
                .filter(TestCaseEvent::isIgnored)
                .map(testCaseEvent -> aTestResult()
                        .withTestClass(testCaseEvent.getTestClass())
                        .withTestMethod(testCaseEvent.getTestMethod())
                        .withIgnored(true)
                        .build())
                .collect(toList());
    }

    private static List<TestResult> getExecutedTests(List<PoolTestResult> poolTestResults,
                                                     List<TestResult> ignoredTestResults) {
        Stream<TestResult> poolTestResultsStream = poolTestResults.stream()
                .map(PoolTestResult::getTestResults)
                .flatMap(Collection::stream);
        Stream<TestResult> ignoredTestResultsStream = ignoredTestResults.stream();
        return concat(poolTestResultsStream, ignoredTestResultsStream).collect(toList());
    }

    private static List<TestResult> getFatalCrashedTests(Collection<TestResult> processedTestResults,
                                                         Collection<TestCaseEvent> testCases) {
        Set<TestResultItem> processedTests = processedTestResults.stream()
                .map(testResult -> new TestResultItem(testResult.getTestClass(), testResult.getTestMethod()))
                .collect(toSet());
        Set<TestResultItem> allTests = testCases.stream()
                .map(testCaseEvent -> new TestResultItem(testCaseEvent.getTestClass(), testCaseEvent.getTestMethod()))
                .collect(toSet());

        return Sets.difference(allTests, processedTests)
                .stream()
                .map(TestResultItem::toTestResult)
                .collect(toList());
    }

    private static final class TestResultItem {
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
