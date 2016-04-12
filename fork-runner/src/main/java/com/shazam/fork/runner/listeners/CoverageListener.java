package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;
import com.shazam.fork.system.io.RemoteFileManager;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shazam.fork.system.io.FileType.COVERAGE;

public class CoverageListener implements ITestRunListener {

    private final Device device;
    private final FileManager fileManager;
    private final Pool pool;
    private final Logger logger = LoggerFactory.getLogger(CoverageListener.class);
    private final TestClass testClass;

    public CoverageListener(Device device, FileManager fileManager, Pool pool, TestClass testClass) {
        this.device = device;
        this.fileManager = fileManager;
        this.pool = pool;
        this.testClass= testClass;
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
    }

    @Override
    public void testStarted(TestIdentifier test) {
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
    }

    @Override
    public void testIgnored(TestIdentifier test) {
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
    }

    @Override
    public void testRunFailed(String errorMessage) {
    }

    @Override
    public void testRunStopped(long elapsedTime) {
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        try {
            final String remoteFile = RemoteFileManager.getCoverageFileName(testClass.getName());
            final File file = fileManager.createFile(pool, device, testClass, COVERAGE);
            device.getDeviceInterface().pullFile(remoteFile, file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Something went wrong while pulling coverage file", e);
        }
    }
}
