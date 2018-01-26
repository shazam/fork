package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.TestCaseEvent;

public interface TestRetryer {
    boolean rescheduleTestExecution(TestIdentifier testIdentifier, TestCaseEvent testCaseEvent);
}
