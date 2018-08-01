package com.shazam.fork.runner;

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
import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static com.shazam.fork.runner.FakePoolTestCaseAccumulator.fakePoolTestCaseAccumulator;
import static com.shazam.fork.runner.FakeProgressReporterTrackers.aFakeProgressReporterTrackers;

public class OverallProgressReporterTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private PoolProgressTracker mockPoolProgressTracker;
    private final FakePoolTestCaseAccumulator fakeTestCasesAccumulator = fakePoolTestCaseAccumulator();

    private final Device A_DEVICE = aDevice().build();
    private final Pool A_POOL = aDevicePool()
            .addDevice(A_DEVICE)
            .build();
    private final TestCaseEvent A_TEST_CASE = testCaseEvent()
            .withTestClass("aTestClass")
            .withTestMethod("aTestMethod")
            .build();

    private OverallProgressReporter overallProgressReporter;

    @Test
    public void requestRetryIsAllowedIfFailedLessThanPermitted() {
        fakeTestCasesAccumulator.thatAlwaysReturnsPoolCount(0);
        overallProgressReporter = new OverallProgressReporter(1, 1,
                aFakeProgressReporterTrackers().thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            oneOf(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

    @Test
    public void requestRetryIsNotAllowedIfFailedMoreThanGloballyPermitted() {
        fakeTestCasesAccumulator.thatAlwaysReturnsTestCaseCount(2);
        overallProgressReporter = new OverallProgressReporter(1, 1,
                aFakeProgressReporterTrackers().thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            never(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

    @Test
    public void requestRetryIsNotAllowedIfFailedMoreThanPermitterPerTest() {
        fakeTestCasesAccumulator.thatAlwaysReturnsTestCaseCount(2);
        overallProgressReporter = new OverallProgressReporter(3, 1,
                aFakeProgressReporterTrackers().thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            never(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }
}
