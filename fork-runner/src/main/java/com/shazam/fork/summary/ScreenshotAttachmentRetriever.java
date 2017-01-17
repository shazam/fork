/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.summary;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.system.io.FileManager;

import java.io.File;
import java.util.List;

import static com.shazam.fork.system.io.FileType.ATTACHMENT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ScreenshotAttachmentRetriever implements AttachmentRetriever {

    private final FileManager fileManager;

    public ScreenshotAttachmentRetriever(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public List<File> retrieveAttachments(String poolName, String safeSerial, TestIdentifier testIdentifier) {
        File[] files = fileManager.getFiles(ATTACHMENT, poolName, safeSerial, testIdentifier);
        return files == null ? emptyList() : asList(files);
    }
}
