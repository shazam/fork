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
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.*;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static com.shazam.fork.system.io.FileType.ATTACHMENT;
import static com.shazam.fork.system.io.RemoteFileManager.remoteScreenshotDirectory;

public class AttachmentListener implements ITestRunListener {
    private final Device device;

    private final FileManager fileManager;
    private final Pool pool;
    private final Logger logger = LoggerFactory.getLogger(CoverageListener.class);
    private final TestCaseEvent testCase;

    public AttachmentListener(Device device, FileManager fileManager, Pool pool, TestCaseEvent testCase) {
        this.device = device;
        this.fileManager = fileManager;
        this.pool = pool;
        this.testCase = testCase;
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
            FileListingService fileListingService = device.getDeviceInterface().getFileListingService();
            FileEntry remoteScreenshotFolder = getRemoteScreenshotFolder(fileListingService);
            if (remoteScreenshotFolder != null) {
                int count = 0;
                FileEntry[] files = fileListingService.getChildrenSync(remoteScreenshotFolder);
                for (FileEntry remoteFile : files) {
                    final File localFile = fileManager.createFile(ATTACHMENT, pool, device, testCase, count++);
                    device.getDeviceInterface().pullFile(remoteFile.getFullPath(), localFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Something went wrong while pulling attached file", e);
        }
    }

    private FileEntry getRemoteScreenshotFolder(FileListingService fileListingService) throws Exception {
        String[] remotePath = remoteScreenshotDirectory(testCase.getTestClass(), testCase.getTestMethod()).split("/");
        FileEntry currentFolder = fileListingService.getRoot();
        for (String file : remotePath) {
            if (file.length() == 0) continue;
            if (currentFolder == null) break;

            fileListingService.getChildrenSync(currentFolder);
            currentFolder = currentFolder.findChild(file);
        }

        return currentFolder;
    }
}
