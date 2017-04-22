package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.*;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.RemoteFileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static com.shazam.fork.system.io.FileType.COVERAGE;

public class CoverageListener implements ITestRunListener {

    private final Device device;
    private final FileManager fileManager;
    private final Pool pool;
    private final Logger logger = LoggerFactory.getLogger(CoverageListener.class);

    public CoverageListener(Device device, FileManager fileManager, Pool pool) {
        this.device = device;
        this.fileManager = fileManager;
        this.pool = pool;
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
        pullCoverageFile(device, pool, fileManager, logger);
    }

    public static void pullCoverageFile(Device device, Pool pool, FileManager fileManager, Logger logger) {
        final String remoteFile = RemoteFileManager.getCoverageFileName(device.getSafeSerial());
        final File file = fileManager.createFile(COVERAGE, pool, device);
        try {
            device.getDeviceInterface().pullFile(remoteFile, file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Something went wrong while pulling coverage file", e);
        }
    }
}
