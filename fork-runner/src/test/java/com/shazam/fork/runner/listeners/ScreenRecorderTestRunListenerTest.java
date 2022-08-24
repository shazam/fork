/*
 * Copyright 2022 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.system.io.FakeFileManager.fakeFileManager;
import static com.shazam.fork.util.TestPipelineEmulator.Builder.testPipelineEmulator;

public class ScreenRecorderTestRunListenerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private ScreenRecorder mockScreenRecorder;

    private final TestIdentifier passingTest = new TestIdentifier("com.example.Test", "testMethod");
    private final TestIdentifier failingTest =
            new TestIdentifier("com.example.FailingTest", "testMethod");

    private File screenRecordingFile;
    private ScreenRecorderTestRunListener listener;

    @Before
    public void setUp() throws Exception {
        screenRecordingFile = temporaryFolder.newFile("screen-recording");
        listener = new ScreenRecorderTestRunListener(
                fakeFileManager().thatSuccessfullyCreatesFile(screenRecordingFile),
                mockScreenRecorder,
                aDevicePool().build(),
                aDevice().build()
        );
    }

    @Test
    public void recordsScreenWhenRunningTests() {
        mockery.checking(new Expectations() {{
            oneOf(mockScreenRecorder).startScreenRecording(passingTest);
            oneOf(mockScreenRecorder).stopScreenRecording(passingTest);

            allowing(mockScreenRecorder).removeScreenRecording(with(any(TestIdentifier.class)));
        }});

        testPipelineEmulator().build().emulateFor(listener, passingTest);
    }

    @Test
    public void persistsScreenRecordingWhenTestFails() {
        mockery.checking(new Expectations() {{
            oneOf(mockScreenRecorder).saveScreenRecording(failingTest, screenRecordingFile);

            allowing(mockScreenRecorder).startScreenRecording(with(any(TestIdentifier.class)));
            allowing(mockScreenRecorder).stopScreenRecording(with(any(TestIdentifier.class)));
            allowing(mockScreenRecorder).removeScreenRecording(with(any(TestIdentifier.class)));
        }});

        testPipelineEmulator()
                .withTestFailed(failingTest, "Error!")
                .build()
                .emulateFor(listener, failingTest);
    }

    @Test
    public void persistsScreenRecordingWhenTestRunFails() {
        mockery.checking(new Expectations() {{
            oneOf(mockScreenRecorder).saveScreenRecording(failingTest, screenRecordingFile);

            allowing(mockScreenRecorder).startScreenRecording(with(any(TestIdentifier.class)));
            allowing(mockScreenRecorder).stopScreenRecording(with(any(TestIdentifier.class)));
            allowing(mockScreenRecorder).removeScreenRecording(with(any(TestIdentifier.class)));
        }});

        testPipelineEmulator()
                .withTestRunFailed("Fatal error!")
                .build()
                .emulateFor(listener, failingTest);
    }

    @Test
    public void removesScreenRecording() {
        mockery.checking(new Expectations() {{
            oneOf(mockScreenRecorder).removeScreenRecording(failingTest);

            allowing(mockScreenRecorder).startScreenRecording(with(any(TestIdentifier.class)));
            allowing(mockScreenRecorder)
                    .saveScreenRecording(with(any(TestIdentifier.class)), with(any(File.class)));
            allowing(mockScreenRecorder).stopScreenRecording(with(any(TestIdentifier.class)));
        }});

        testPipelineEmulator()
                .withTestFailed(failingTest, "Error!")
                .build()
                .emulateFor(listener, failingTest);
    }
}
