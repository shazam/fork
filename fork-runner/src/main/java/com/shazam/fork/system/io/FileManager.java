/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.system.io;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.*;

import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.nio.file.Path;

import static com.shazam.fork.CommonDefaults.FORK_SUMMARY_FILENAME_FORMAT;
import static com.shazam.fork.system.io.FileType.TEST;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;

public class FileManager {
    private final File output;

    public FileManager(File output) {
        this.output = output;
    }

    public File[] getTestFilesForDevice(Pool pool, Device serial) {
        Path path = getDirectory(TEST, pool, serial);
        return path.toFile().listFiles();
    }

    public File createFile(FileType fileType, Pool pool, Device device, TestCaseEvent testCaseEvent) {
        return createFile(fileType, pool, device, new TestIdentifier(testCaseEvent.getTestClass(), testCaseEvent.getTestMethod()));
    }

    public File createFile(FileType fileType, Pool pool, Device device, TestCaseEvent testCaseEvent, int sequenceNumber) {
        return createFile(fileType, pool, device, new TestIdentifier(testCaseEvent.getTestClass(), testCaseEvent.getTestMethod()),
                sequenceNumber);
    }

    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier, int sequenceNumber) {
        try {
            Path directory = createDirectory(fileType, pool, device);
            String filename = createFilenameForTest(testIdentifier, fileType, sequenceNumber);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        try {
            Path directory = createDirectory(fileType, pool, device);
            String filename = createFilenameForTest(testIdentifier, fileType);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File createSummaryFile() {
        try {
            Path path = get(output.getAbsolutePath(), "summary");
            Path directory = createDirectories(path);
            return createFile(directory, String.format(FORK_SUMMARY_FILENAME_FORMAT, System.currentTimeMillis()));
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File[] getFiles(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        return getFiles(fileType, pool.getName(), device.getSafeSerial(), testIdentifier);
    }

    public File[] getFiles(FileType fileType, String pool, String safeSerial, TestIdentifier testIdentifier) {
        FileFilter fileFilter = new AndFileFilter(
                new PrefixFileFilter(testIdentifier.toString()),
                new SuffixFileFilter(fileType.getSuffix()));

        File deviceDirectory = get(output.getAbsolutePath(), fileType.getDirectory(), pool, safeSerial).toFile();
        return deviceDirectory.listFiles(fileFilter);
    }

    public File getFile(FileType fileType, String pool, String safeSerial, TestIdentifier testIdentifier) {
        String filenameForTest = createFilenameForTest(testIdentifier, fileType);
        Path path = get(output.getAbsolutePath(), fileType.getDirectory(), pool, safeSerial, filenameForTest);
        return path.toFile();
    }

    private Path createDirectory(FileType test, Pool pool, Device device) throws IOException {
        return createDirectories(getDirectory(test, pool, device));
    }

    private Path getDirectory(FileType fileType, Pool pool, Device device) {
        return get(output.getAbsolutePath(), fileType.getDirectory(), pool.getName(), device.getSafeSerial());
    }

    private File createFile(Path directory, String filename) {
        return new File(directory.toFile(), filename);
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType) {
        return String.format("%s.%s", testIdentifier.toString(), fileType.getSuffix());
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType, int sequenceNumber) {
        return String.format("%s-%02d.%s", testIdentifier.toString(), sequenceNumber, fileType.getSuffix());
    }
}
