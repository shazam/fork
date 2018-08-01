package com.shazam.fork.model;

import org.junit.Before;
import org.junit.Test;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PoolTestCaseAccumulatorTestFailure {
    private final Device A_DEVICE = aDevice()
            .withSerial("a_device")
            .build();
    private final Device ANOTHER_DEVICE = aDevice()
            .withSerial("another_device")
            .build();

    private final Pool A_POOL = aDevicePool()
            .withName("a_pool")
            .addDevice(A_DEVICE)
            .build();
    private final Pool ANOTHER_POOL = aDevicePool()
            .withName("another_pool")
            .addDevice(ANOTHER_DEVICE)
            .build();

    private final TestCaseEvent A_TEST_CASE = testCaseEvent()
            .withTestClass("a_class")
            .withTestMethod("a_method")
            .build();
    private final TestCaseEvent ANOTHER_TEST_CASE = testCaseEvent()
            .withTestClass("a_class")
            .withTestMethod("another_method")
            .build();

    private PoolTestCaseFailureAccumulator subject;

    @Before
    public void setUp() {
        subject = new PoolTestCaseFailureAccumulator();
    }

    @Test
    public void shouldAggregateCountForSameTestCaseAcrossMultipleDevices() {
        subject.record(A_POOL, A_TEST_CASE);
        subject.record(A_POOL, A_TEST_CASE);

        int actualCount = subject.getCount(A_TEST_CASE);

        assertThat(actualCount, equalTo(2));
    }

    @Test
    public void shouldCountTestsPerPool() {
        subject.record(A_POOL, A_TEST_CASE);
        subject.record(A_POOL, A_TEST_CASE);

        int actualCount = subject.getCount(A_POOL, A_TEST_CASE);

        assertThat(actualCount, equalTo(2));
    }

    @Test
    public void shouldAggregateCountForSameTestCaseAcrossMultiplePools() {
        subject.record(A_POOL, A_TEST_CASE);
        subject.record(ANOTHER_POOL, A_TEST_CASE);

        int actualCount = subject.getCount(A_TEST_CASE);

        assertThat(actualCount, equalTo(2));
    }

    @Test
    public void shouldNotReturnTestCasesForDifferentPool() {
        subject.record(A_POOL, A_TEST_CASE);

        int actualCountForAnotherDevice = subject.getCount(ANOTHER_POOL, A_TEST_CASE);

        assertThat(actualCountForAnotherDevice, equalTo(0));
    }

    @Test
    public void shouldAccumulateDifferentTestCasesForSamePool() {
        subject.record(A_POOL, A_TEST_CASE);
        subject.record(A_POOL, ANOTHER_TEST_CASE);

        int actualCount = subject.getCount(A_POOL, A_TEST_CASE);
        int anotherActualCount = subject.getCount(A_POOL, ANOTHER_TEST_CASE);

        assertThat(actualCount, equalTo(1));
        assertThat(anotherActualCount, equalTo(1));
    }
}
