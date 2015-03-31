/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.system.io;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.TestClass;

import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.nio.file.Path;

import static com.shazam.fork.system.io.FileType.TEST;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;

public class FileManager {
    private final File output;

    public FileManager(File output) {
        this.output = output;
    }

    public File createFileForTest(String pool, String deviceSerial, TestClass testClass) {
        try {
            Path directory = createDirectory(TEST, pool, deviceSerial);
            String filename = createFilenameForTestClass(testClass, TEST);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File[] getTestFilesForDevice(String pool, String serial) {
        Path path = getDirectory(TEST, pool, serial);
        return path.toFile().listFiles();
    }

    public File createFile(FileType fileType, String pool, String serial, TestIdentifier testIdentifier, int sequenceNumber) {
        try {
            Path directory = createDirectory(fileType, pool, serial);
            String filename = createFilenameForTest(testIdentifier, fileType, sequenceNumber);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File createFile(FileType fileType, String pool, String serial, TestIdentifier testIdentifier) {
        try {
            Path directory = createDirectory(fileType, pool, serial);
            String filename = createFilenameForTest(testIdentifier, fileType);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    public File[] getFiles(FileType fileType, String pool, String serial, TestIdentifier testIdentifier) {
        FileFilter fileFilter = new AndFileFilter(
                new PrefixFileFilter(testIdentifier.toString()),
                new SuffixFileFilter(fileType.getSuffix()));

        File deviceDirectory = get(output.getAbsolutePath(), fileType.getDirectory(), pool, serial).toFile();
        return deviceDirectory.listFiles(fileFilter);
    }

    public File getFile(FileType fileType, String pool, String serial, TestIdentifier testIdentifier) {
        String filenameForTest = createFilenameForTest(testIdentifier, fileType);
        Path path = get(output.getAbsolutePath(), fileType.getDirectory(), pool, serial, filenameForTest);
        return path.toFile();
    }

    private Path createDirectory(FileType test, String pool, String deviceSerial) throws IOException {
        return createDirectories(getDirectory(test, pool, deviceSerial));
    }

    private Path getDirectory(FileType fileType, String pool, String deviceSerial) {
        return get(output.getAbsolutePath(), fileType.getDirectory(), pool, deviceSerial);
    }

    private File createFile(Path directory, String filename) {
        return new File(directory.toFile(), filename);
    }

    private String createFilenameForTestClass(TestClass testClass, FileType fileType) {
        return testClass.getName() + "." + fileType.getSuffix();
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType) {
        return String.format("%s.%s", testIdentifier.toString(), fileType.getSuffix());
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType, int sequenceNumber) {
        return String.format("%s-%02d.%s", testIdentifier.toString(), sequenceNumber, fileType.getSuffix());
    }
}
