package com.shazam.fork.runner;

import com.shazam.fork.model.TestCaseEvent;

public interface FailedTestScheduler {
    boolean rescheduleTestExecution(TestCaseEvent testCaseEvent);
}
