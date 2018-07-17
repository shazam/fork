package com.shazam.fork.device;

import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;

public class FileManagerBasedDeviceTestFilesCleaner implements DeviceTestFilesCleaner {
    private static final Logger logger = LoggerFactory.getLogger(FileManagerBasedDeviceTestFilesCleaner.class);
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;

    public FileManagerBasedDeviceTestFilesCleaner(FileManager fileManager, Pool pool, Device device) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
    }

    @Override
    public boolean deleteTraceFiles(@Nonnull TestCaseEvent testCase) {
        File file = fileManager.getFile(FileType.TEST, pool, device, testCase);
        boolean isDeleted = file.delete();
        if (!isDeleted) {
            logger.warn("Failed to delete a file %s", file.getAbsolutePath());
        }
        return isDeleted;
    }
}
