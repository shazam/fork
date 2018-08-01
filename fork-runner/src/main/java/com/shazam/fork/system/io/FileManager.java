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
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import javax.annotation.Nonnull;
import java.io.File;

public interface FileManager {
    File[] getTestFilesForDevice(Pool pool, Device serial);

    File createFile(FileType fileType, Pool pool, Device device, TestCaseEvent testCaseEvent);

    File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier, int sequenceNumber);

    File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier);

    File createSummaryFile();

    File[] getFiles(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier);

    File getFile(FileType fileType, String pool, String safeSerial, TestIdentifier testIdentifier);

    File getFile(@Nonnull FileType fileType,
                 @Nonnull Pool pool,
                 @Nonnull Device device,
                 @Nonnull TestCaseEvent testCase);
}