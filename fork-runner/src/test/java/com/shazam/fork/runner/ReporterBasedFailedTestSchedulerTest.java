/*
 * Copyright 2018 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static com.shazam.fork.runner.FakeProgressReporter.fakeProgressReporter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ReporterBasedFailedTestSchedulerTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock
    private ProgressReporter mockProgressReporter;

    private final FakeProgressReporter fakeProgressReporter = fakeProgressReporter();

    private Queue<TestCaseEvent> testsQueue;
    private Pool pool = aDevicePool()
            .withName("aName")
            .build();

    private ReporterBasedFailedTestScheduler testScheduler;
    private TestCaseEvent testCase = testCaseEvent()
            .withTestClass("com.shazam.fork.TestClass")
            .withTestMethod("testMethod")
            .build();

    @Before
    public void setUp() {
        testsQueue = new LinkedList<>();
    }

    @Test
    public void recordsFailedTestCase() {
        testScheduler = new ReporterBasedFailedTestScheduler(mockProgressReporter, pool, testsQueue);

        mockery.checking(new Expectations() {{
            oneOf(mockProgressReporter).recordFailedTestCase(pool, testCase);

            allowing(mockProgressReporter).requestRetry(pool, testCase);
        }});

        testScheduler.rescheduleTestExecution(testCase);
    }

    @Test
    public void addsTestCaseToQueueWhenRetryIsAllowed() {
        fakeProgressReporter.thatAlwaysAllowTestToBeRescheduled();
        testScheduler = new ReporterBasedFailedTestScheduler(fakeProgressReporter, pool, testsQueue);

        testScheduler.rescheduleTestExecution(testCase);

        assertThat(testsQueue, contains(testCase));
    }

    @Test
    public void returnsTrueWhenTestCaseIsRescheduled() {
        fakeProgressReporter.thatAlwaysAllowTestToBeRescheduled();
        testScheduler = new ReporterBasedFailedTestScheduler(fakeProgressReporter, pool, testsQueue);

        assertThat(testScheduler.rescheduleTestExecution(testCase), equalTo(true));
    }

    @Test
    public void doesNotAddTestCaseToQueueWhenRetryIsNotAllowed() {
        fakeProgressReporter.thatAlwaysDisallowTestToBeRescheduled();
        testScheduler = new ReporterBasedFailedTestScheduler(fakeProgressReporter, pool, testsQueue);

        testScheduler.rescheduleTestExecution(testCase);

        assertThat(testsQueue, not(contains(testCase)));
    }

    @Test
    public void returnsFalseWhenTestCaseIsNotRescheduled() {
        fakeProgressReporter.thatAlwaysDisallowTestToBeRescheduled();
        testScheduler = new ReporterBasedFailedTestScheduler(fakeProgressReporter, pool, testsQueue);

        assertThat(testScheduler.rescheduleTestExecution(testCase), equalTo(false));
    }
}