package com.shazam.fork.injector.summary;

import com.shazam.fork.summary.DeviceTestFilesRetriever;
import com.shazam.fork.summary.ForkDeviceTestFilesRetriever;
import org.simpleframework.xml.core.Persister;

import static com.shazam.fork.injector.system.FileManagerInjector.fileManager;

public class DeviceTestFilesRetrieverInjector {
    private DeviceTestFilesRetrieverInjector() {
        throw new AssertionError("No instances");
    }

    public static DeviceTestFilesRetriever deviceTestFilesRetriever() {
        return new ForkDeviceTestFilesRetriever(fileManager(), new Persister());
    }
}
