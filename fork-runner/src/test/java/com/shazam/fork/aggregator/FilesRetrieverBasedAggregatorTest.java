package com.shazam.fork.aggregator;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.summary.FakeDeviceTestFilesRetriever;
import com.shazam.fork.summary.TestResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.newTestCase;
import static com.shazam.fork.summary.FakeDeviceTestFilesRetriever.aFakeDeviceTestFilesRetriever;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static com.shazam.fork.summary.TestResult.SUMMARY_KEY_TOTAL_FAILURE_COUNT;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public class FilesRetrieverBasedAggregatorTest {
    private final Device ignoredDevice = aDevice().build();
    private final Collection<Pool> devicePools = newArrayList(
            aDevicePool()
                    .addDevice(ignoredDevice)
                    .build()
    );

    private static final HashMap<String, String> TEST_METRICS_FOR_FAILED_TEST = new HashMap<>();

    static {
        TEST_METRICS_FOR_FAILED_TEST.put(SUMMARY_KEY_TOTAL_FAILURE_COUNT, "10");
    }

    private final TestResult completedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(10.0f)
            .build();
    private final TestResult anotherCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest2")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(15.0f)
            .build();
    private final TestResult failedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.FailedClassTest")
            .withTestMethod("doesJobProperly")
            .withFailureTrace("a failure stacktrace")
            .withTestMetrics(TEST_METRICS_FOR_FAILED_TEST)
            .build();
    private final TestResult ignoreTest = aTestResult()
            .withTestClass("com.example.IgnoredClassTest")
            .withTestMethod("doesJobProperly")
            .withIgnored(true)
            .build();
    private final TestResult fatalCrashedTest = aTestResult()
            .withTestClass("com.example.FatalCrashedTest")
            .withTestMethod("doesJobProperly")
            .withIgnored(false)
            .build();

    private final Collection<TestResult> testResults = newArrayList(
            completedTest,
            anotherCompletedTest,
            failedTest,
            ignoreTest
    );

    private final Collection<TestCaseEvent> testCaseEvents = newArrayList(
            newTestCase(new TestIdentifier("com.example.CompletedClassTest", "doesJobProperly")),
            newTestCase(new TestIdentifier("com.example.CompletedClassTest2", "doesJobProperly")),
            newTestCase("doesJobProperly", "com.example.FailedClassTest", false,
                    emptyList(), TEST_METRICS_FOR_FAILED_TEST),
            newTestCase(new TestIdentifier("com.example.IgnoredClassTest", "doesJobProperly"), true),
            newTestCase(new TestIdentifier("com.example.FatalCrashedTest", "doesJobProperly"))
    );

    private final FakeDeviceTestFilesRetriever fakeDeviceTestFilesRetriever = aFakeDeviceTestFilesRetriever();
    private FilesRetrieverBasedAggregator aggregator;

    @Before
    public void setUp() {
        aggregator = new FilesRetrieverBasedAggregator(fakeDeviceTestFilesRetriever);
    }

    @Test
    public void aggregatesCompletedTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        AggregatedTestResult aggregatedTestResult = aggregator.aggregateTestResults(devicePools, testCaseEvents);

        assertThat(aggregatedTestResult.getPoolTestResults().get(0).getTestResults(),
                hasItems(
                        completedTest,
                        anotherCompletedTest
                )
        );
    }

    @Test
    public void aggregatesIgnoredTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        AggregatedTestResult aggregatedTestResult = aggregator.aggregateTestResults(devicePools, testCaseEvents);

        assertThat(aggregatedTestResult.getIgnoredTests(), contains(ignoreTest));
    }

    @Test
    public void aggregatesFailedTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        AggregatedTestResult aggregatedTestResult = aggregator.aggregateTestResults(devicePools, testCaseEvents);

        assertThat(aggregatedTestResult.getPoolTestResults().get(0).getTestResults(), hasItem(failedTest));
    }

    @Test
    public void aggregatesFatalCrashedTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        AggregatedTestResult aggregatedTestResult = aggregator.aggregateTestResults(devicePools, testCaseEvents);

        assertThat(aggregatedTestResult.getFatalCrashedTests(), contains(fatalCrashedTest));
    }
}