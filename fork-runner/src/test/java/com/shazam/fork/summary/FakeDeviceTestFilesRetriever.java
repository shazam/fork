package com.shazam.fork.summary;

import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;

import javax.annotation.Nonnull;
import java.util.Collection;

import static java.util.Collections.emptyList;

public class FakeDeviceTestFilesRetriever implements DeviceTestFilesRetriever {
    private Collection<TestResult> testResults = emptyList();

    private FakeDeviceTestFilesRetriever() {
    }

    public static FakeDeviceTestFilesRetriever aFakeDeviceTestFilesRetriever() {
        return new FakeDeviceTestFilesRetriever();
    }

    public FakeDeviceTestFilesRetriever thatReturns(@Nonnull Collection<TestResult> testResults) {
        this.testResults = testResults;
        return this;
    }

    @Nonnull
    @Override
    public Collection<TestResult> getTestResultsForDevice(Pool pool, Device device) {
        return testResults;
    }
}
