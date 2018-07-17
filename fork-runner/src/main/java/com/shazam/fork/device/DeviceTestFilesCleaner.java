package com.shazam.fork.device;

import com.shazam.fork.model.TestCaseEvent;

import javax.annotation.Nonnull;

public interface DeviceTestFilesCleaner {
    boolean deleteTraceFiles(@Nonnull TestCaseEvent testCase);
}
