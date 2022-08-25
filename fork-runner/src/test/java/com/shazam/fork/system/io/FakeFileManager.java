/*
 * Copyright 2022 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shazam.fork.system.io;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FakeFileManager implements FileManager {
    private File createdFile;

    public static FakeFileManager fakeFileManager() {
        return new FakeFileManager();
    }

    private FakeFileManager() {
    }

    public FileManager thatSuccessfullyCreatesFile(File createdFile) {
        this.createdFile = createdFile;
        return this;
    }

    @Override
    public File[] getTestFilesForDevice(Pool pool, Device serial) {
        return new File[0];
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestCaseEvent testCaseEvent) {
        return createdFile;
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier, int sequenceNumber) {
        return createdFile;
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        return createdFile;
    }

    @Override
    public File createSummaryFile() {
        return null;
    }

    @Override
    public File[] getFiles(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        return new File[0];
    }

    @Override
    public File getFile(FileType fileType, String pool, String safeSerial, TestIdentifier testIdentifier) {
        return null;
    }

    @Override
    public File getFile(@NotNull FileType fileType, @NotNull Pool pool, @NotNull Device device, @NotNull TestIdentifier testIdentifier) {
        return null;
    }
}
