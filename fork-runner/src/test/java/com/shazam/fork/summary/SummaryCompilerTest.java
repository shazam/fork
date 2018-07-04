package com.shazam.fork.summary;

import com.shazam.fork.aggregator.AggregatedTestResult;
import com.shazam.fork.model.Device;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.shazam.fork.FakeForkConfiguration.aFakeForkConfiguration;
import static com.shazam.fork.aggregator.AggregatedTestResult.Builder.aggregatedTestResult;
import static com.shazam.fork.aggregator.PoolTestResult.Builder.poolTestResult;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static com.shazam.fork.summary.TestResult.SUMMARY_KEY_TOTAL_FAILURE_COUNT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class SummaryCompilerTest {
    private SummaryCompiler summaryCompiler;

    private final Device ignoredDevice = aDevice().build();

    private final HashMap<String, String> testMetricsForFailedTest = new HashMap<String, String>() {{
        put(SUMMARY_KEY_TOTAL_FAILURE_COUNT, "10");
    }};

    private final TestResult firstCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(10f)
            .build();
    private final TestResult secondCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest2")
            .withTestMethod("doesJobProperly")
            .withTimeTaken(15f)
            .build();
    private final TestResult thirdCompletedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.CompletedClassTest3")
            .withTestMethod("doesJobPropertly")
            .withTimeTaken(5f)
            .build();
    private final TestResult failedTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.FailedClassTest")
            .withTestMethod("doesJobProperly")
            .withFailureTrace("a failure stacktrace")
            .withTestMetrics(testMetricsForFailedTest)
            .build();
    private final TestResult ignoredTest = aTestResult()
            .withDevice(ignoredDevice)
            .withTestClass("com.example.IgnoredClassTest")
            .withTestMethod("doesJobProperly")
            .withIgnored(true)
            .build();
    private final TestResult fatalCrashedTest = aTestResult()
            .withTestClass("com.example.FatalCrashedTest")
            .withTestMethod("doesJobProperly")
            .build();

    @Before
    public void setUp() {
        summaryCompiler = new SummaryCompiler(aFakeForkConfiguration());
    }

    @Test
    public void compilesSummaryWithCompletedTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withPoolTestResults(asList(
                        poolTestResult()
                                .withPool(aDevicePool().withName("aPool").build())
                                .withTestResults(
                                        asList(
                                                firstCompletedTest,
                                                secondCompletedTest
                                        )
                                )
                                .build(),
                        poolTestResult()
                                .withPool(aDevicePool().withName("anotherPool").build())
                                .withTestResults(singletonList(thirdCompletedTest))
                                .build()
                ))
                .build();

        Summary summary = summaryCompiler.compileSummary(aggregatedTestResult);

        assertThat(summary.getPoolSummaries().get(0).getTestResults(),
                containsInAnyOrder(
                        firstCompletedTest,
                        secondCompletedTest
                )
        );
        assertThat(summary.getPoolSummaries().get(1).getTestResults(), contains(thirdCompletedTest));
    }

    @Test
    public void compilesSummaryWithIgnoredTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withIgnoredTests(singletonList(ignoredTest))
                .build();

        Summary summary = summaryCompiler.compileSummary(aggregatedTestResult);

        assertThat(summary.getIgnoredTests(), hasSize(1));
        assertThat(summary.getIgnoredTests(), contains("com.example.IgnoredClassTest:doesJobProperly"));
    }

    @Test
    public void compilesSummaryWithFailedTests() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withPoolTestResults(singletonList(
                        poolTestResult()
                                .withPool(aDevicePool().withName("aPool").build())
                                .withTestResults(singletonList(failedTest))
                                .build()
                ))
                .build();

        Summary summary = summaryCompiler.compileSummary(aggregatedTestResult);

        assertThat(summary.getFailedTests(), hasSize(1));
        assertThat(summary.getFailedTests(),
                contains("10 times com.example.FailedClassTest#doesJobProperly on Unspecified serial"));
    }

    @Test
    public void compilesSummaryWithFatalCrashedTestsIfTheyAreNotFoundInPassedOrFailedOrIgnored() {
        AggregatedTestResult aggregatedTestResult = aggregatedTestResult()
                .withFatalCrashedTests(singletonList(fatalCrashedTest))
                .build();

        Summary summary = summaryCompiler.compileSummary(aggregatedTestResult);

        assertThat(summary.getFatalCrashedTests(), hasSize(1));
        assertThat(summary.getFatalCrashedTests(),
                contains("com.example.FatalCrashedTest#doesJobProperly on Unknown device"));
    }
}