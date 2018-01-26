package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.device.DeviceTestFilesCleaner;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.TestRetryer;
import com.shazam.fork.util.TestPipelineEmulator;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.newTestCase;
import static com.shazam.fork.util.TestPipelineEmulator.Builder.testPipelineEmulator;

public class RetryListenerTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private TestRetryer testRetryer;
    @Mock
    private DeviceTestFilesCleaner deviceTestFilesCleaner;

    private final Device device = aDevice().build();
    private final Pool pool = aDevicePool()
            .withName("pool")
            .addDevice(device)
            .build();

    private final TestIdentifier fatalCrashedTest = new TestIdentifier("com.example.FatalCrashedTest", "testMethod");
    private final TestCaseEvent fatalCrashedTestCaseEvent = newTestCase(fatalCrashedTest);

    @Test
    public void reschedulesTestIfTestRunFailedAndDeleteTraceFiles() {
        RetryListener retryListener =
                new RetryListener(pool, device, fatalCrashedTestCaseEvent, testRetryer, deviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            oneOf(testRetryer).rescheduleTestExecution(fatalCrashedTest, fatalCrashedTestCaseEvent);
            will(returnValue(true));

            oneOf(deviceTestFilesCleaner).deleteTraceFiles(fatalCrashedTest);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withFatalErrorMessage("fatal error")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }

    @Test
    public void doesNotDeleteTraceFilesIfCannotRescheduleTestAfterTestRunFailed() {
        RetryListener retryListener =
                new RetryListener(pool, device, fatalCrashedTestCaseEvent, testRetryer, deviceTestFilesCleaner);

        mockery.checking(new Expectations() {{
            oneOf(testRetryer).rescheduleTestExecution(fatalCrashedTest, fatalCrashedTestCaseEvent);
            will(returnValue(false));

            never(deviceTestFilesCleaner).deleteTraceFiles(fatalCrashedTest);
        }});

        TestPipelineEmulator emulator = testPipelineEmulator()
                .withFatalErrorMessage("fatal error")
                .build();
        emulator.emulateFor(retryListener, fatalCrashedTest);
    }
}