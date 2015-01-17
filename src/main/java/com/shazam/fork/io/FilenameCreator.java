/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.io;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.shazam.fork.io.FileType.TEST;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;

public class FilenameCreator {
    private final File output;

    public FilenameCreator(File output) {
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

    public File createFile(FileType fileType, String pool, String deviceSerial, TestIdentifier testIdentifier) {
        try {
            Path directory = createDirectory(fileType, pool, deviceSerial);
            String filename = createFilenameForTest(testIdentifier, fileType);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    private Path createDirectory(FileType test, String pool, String deviceSerial) throws IOException {
        return createDirectories(get(output.getAbsolutePath(), test.getDirectory(), pool, deviceSerial));
    }

    private File createFile(Path directory, String filename) {
        return new File(directory.toFile(), filename);
    }

    private String createFilenameForTestClass(TestClass testClass, FileType fileType) {
        return testClass.getClassName() + "." + fileType.getSuffix();
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType) {
        return testIdentifier.toString() + "." + fileType.getSuffix();
    }
}
