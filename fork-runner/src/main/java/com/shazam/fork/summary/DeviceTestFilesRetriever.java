package com.shazam.fork.summary;

import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface DeviceTestFilesRetriever {
    @Nonnull
    Collection<TestResult> getTestResultsForDevice(Pool pool, Device device);
}
