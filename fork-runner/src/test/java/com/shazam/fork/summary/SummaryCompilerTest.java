package com.shazam.fork.summary;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.ForkConfiguration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class SummaryCompilerTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private ForkConfiguration mockConfiguration;

    private final FakeDeviceTestFilesRetriever fakeDeviceTestFilesRetriever = aFakeDeviceTestFilesRetriever();
    private SummaryCompiler summaryCompiler;

    private final Device ignoredDevice = aDevice().build();
    private final Collection<Pool> devicePools = newArrayList(
            aDevicePool()
                    .addDevice(ignoredDevice)
                    .build()
    );

    private final TestResult firstCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(10.0f)
            .build();
    private final TestResult secondCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest2")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(15.0f)
            .build();

    private final HashMap<String, String> testMetricsForFailedTest = new HashMap<String, String>() {{
        put(SUMMARY_KEY_TOTAL_FAILURE_COUNT, "10");
    }};
    private final Collection<TestResult> testResults = newArrayList(
            firstCompletedTest,
            secondCompletedTest,
            aTestResult()
                    .withDevice(ignoredDevice)
                    .withTestClass("com.example.FailedClassTest")
                    .withTestMethod("doesJobProperly")
                    .withFailureTrace("a failure stacktrace")
                    .withTestMetrics(testMetricsForFailedTest)
                    .build(),
            aTestResult()
                    .withDevice(ignoredDevice)
                    .withTestClass("com.example.IgnoredClassTest")
                    .withTestMethod("doesJobProperly")
                    .withIgnored(true)
                    .build()
    );

    private final Collection<TestCaseEvent> testCaseEvents = newArrayList(
            newTestCase(new TestIdentifier("com.example.CompletedClassTest", "doesJobProperly")),
            newTestCase(new TestIdentifier("com.example.CompletedClassTest2", "doesJobProperly")),
            newTestCase("doesJobProperly", "com.example.FailedClassTest", false,
                    emptyList(), testMetricsForFailedTest),
            newTestCase(new TestIdentifier("com.example.IgnoredClassTest", "doesJobProperly"), true),
            newTestCase(new TestIdentifier("com.example.SkippedClassTest", "doesJobProperly"))
    );

    @Before
    public void setUp() {
        summaryCompiler = new SummaryCompiler(mockConfiguration, fakeDeviceTestFilesRetriever);
        mockery.checking(new Expectations() {{
            allowing(mockConfiguration);
        }});
    }

    @Test
    public void compilesSummaryWithCompletedTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        Summary summary = summaryCompiler.compileSummary(devicePools, testCaseEvents);

        assertThat(summary.getPoolSummaries().get(0).getTestResults(), hasItems(
                firstCompletedTest, secondCompletedTest));
    }

    @Test
    public void compilesSummaryWithIgnoredTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        Summary summary = summaryCompiler.compileSummary(devicePools, testCaseEvents);

        assertThat(summary.getIgnoredTests(), hasSize(1));
        assertThat(summary.getIgnoredTests(), contains("com.example.IgnoredClassTest:doesJobProperly"));
    }

    @Test
    public void compilesSummaryWithFailedTests() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        Summary summary = summaryCompiler.compileSummary(devicePools, testCaseEvents);

        assertThat(summary.getFailedTests(), hasSize(1));
        assertThat(summary.getFailedTests(),
                contains("10 times com.example.FailedClassTest#doesJobProperly on Unspecified serial"));
    }

    @Test
    public void compilesSummaryWithSkippedTestsIfTheyAreNotFoundInPassedOrFailedOrIgnored() {
        fakeDeviceTestFilesRetriever.thatReturns(testResults);

        Summary summary = summaryCompiler.compileSummary(devicePools, testCaseEvents);

        assertThat(summary.getSkippedTests(), hasSize(1));
        assertThat(summary.getSkippedTests(), contains("com.example.SkippedClassTest:doesJobProperly"));
    }
}