package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.device.DeviceTestFilesCleaner;
import com.shazam.fork.device.FakeDeviceTestFilesCleaner;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.FailedTestScheduler;
import com.shazam.fork.util.TestPipelineEmulator;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.shazam.fork.device.FakeDeviceTestFilesCleaner.fakeDeviceTestFilesCleaner;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.util.TestPipelineEmulator.Builder.testPipelineEmulator;

public class RetryListenerTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private FailedTestScheduler mockFailedTestScheduler;
    @Mock
    private DeviceTestFilesCleaner mockDeviceTestFilesCleaner;

    private final FakeDeviceTestFilesCleaner fakeDeviceTestFilesCleaner = fakeDeviceTestFilesCleaner();

    private final Device device = aDevice().build();
    private final Pool pool = aDevicePool()
            .withName("pool")
            .addDevice(device)
            .build();

    private final TestIdentifier fatalCrashedTest = new TestIdentifier("com.example.FatalCrashedTest", "testMethod");
    private final TestCaseEvent fatalCrashedTestCaseEvent = TestCaseEvent.from(fatalCrashedTest);

    @Test
    public void reschedulesTestAndDeletesTraceFilesWhenRunFailed() {
        RetryListener retryListener =
                new RetryListener(pool, device, mockFailedTestScheduler, mockDeviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            oneOf(mockFailedTestScheduler).rescheduleTestExecution(fatalCrashedTestCaseEvent);
            will(returnValue(true));

            oneOf(mockDeviceTestFilesCleaner).deleteTraceFiles(fatalCrashedTestCaseEvent);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withTestFailed("assert exception")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }

    @Test
    public void doesNotDeleteTraceFilesIfCannotRescheduleTestWhenRunFailed() {
        RetryListener retryListener =
                new RetryListener(pool, device, mockFailedTestScheduler, mockDeviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            oneOf(mockFailedTestScheduler).rescheduleTestExecution(fatalCrashedTestCaseEvent);
            will(returnValue(false));

            never(mockDeviceTestFilesCleaner).deleteTraceFiles(fatalCrashedTestCaseEvent);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withTestFailed("assert exception")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }

    @Test
    public void reschedulesTestWhenTestFailsAndThenTestRunCrashes() {
        RetryListener retryListener =
                new RetryListener(pool, device, mockFailedTestScheduler, fakeDeviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            oneOf(mockFailedTestScheduler).rescheduleTestExecution(fatalCrashedTestCaseEvent);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withTestFailed("assert exception")
                .withTestRunFailed("fatal error")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }

    @Test
    public void doesNotRescheduleTestWhenTestRunFailsWithoutCrash() {
        RetryListener retryListener =
                new RetryListener(pool, device, mockFailedTestScheduler, fakeDeviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            never(mockFailedTestScheduler);
            never(mockDeviceTestFilesCleaner);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withTestRunFailed("fatal error")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }
}