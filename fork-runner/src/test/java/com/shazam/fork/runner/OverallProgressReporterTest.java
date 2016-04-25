package com.shazam.fork.runner;

import com.shazam.fork.Configuration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.newTestCase;
import static com.shazam.fork.runner.FakePoolTestCaseAccumulator.aFakePoolTestCaseAccumulator;
import static com.shazam.fork.runner.FakeProgressReporterTrackers.aFakeProgressReporterTrackers;

public class OverallProgressReporterTest {

    @Rule public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock private PoolProgressTracker mockPoolProgressTracker;
    private FakePoolTestCaseAccumulator fakeTestCasesAccumulator = aFakePoolTestCaseAccumulator();

    private final Device A_DEVICE = aDevice().build();
    private final Pool A_POOL = aDevicePool()
            .addDevice(A_DEVICE)
            .build();
    private final TestCaseEvent A_TEST_CASE = newTestCase("aTestMethod", "aTestClass", false);

    private OverallProgressReporter overallProgressReporter;

    @Test
    public void requestRetryIsAllowedIfFailedLessThanPermitted() throws Exception {
        Configuration configuration = aConfiguration(1, 1);
        fakeTestCasesAccumulator.thatAlwaysReturns(0);
        overallProgressReporter = new OverallProgressReporter(configuration,
                aFakeProgressReporterTrackers()
                        .thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            oneOf(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

    @Test
    public void requestRetryIsNotAllowedIfFailedMoreThanPermitted() throws Exception {
        Configuration configuration = aConfiguration(1, 1);
        fakeTestCasesAccumulator.thatAlwaysReturns(2);
        overallProgressReporter = new OverallProgressReporter(configuration,
                aFakeProgressReporterTrackers()
                        .thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            never(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

    private Configuration aConfiguration(int totalRetry, int singleMethodRetry) {
        return new Configuration(null, null, null, null, null, null, null, null, 0, false, totalRetry, singleMethodRetry, false, true);
    }
}
