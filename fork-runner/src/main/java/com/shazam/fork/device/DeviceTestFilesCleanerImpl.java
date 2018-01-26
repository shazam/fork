package com.shazam.fork.device;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DeviceTestFilesCleanerImpl implements DeviceTestFilesCleaner {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestFilesCleanerImpl.class);
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;

    public DeviceTestFilesCleanerImpl(FileManager fileManager, Pool pool, Device device) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
    }

    @Override
    public boolean deleteTraceFiles(TestIdentifier testIdentifier) {
        File file = fileManager.getFile(FileType.TEST, pool.getName(), device.getSafeSerial(), testIdentifier);
        boolean isDeleted = file.delete();
        if (!isDeleted) {
            logger.warn("Failed to delete a file %s", file.getAbsolutePath());
        }
        return isDeleted;
    }
}
