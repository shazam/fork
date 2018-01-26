package com.shazam.fork.device;

import com.android.ddmlib.testrunner.TestIdentifier;

public interface DeviceTestFilesCleaner {
    boolean deleteTraceFiles(TestIdentifier testIdentifier);
}
