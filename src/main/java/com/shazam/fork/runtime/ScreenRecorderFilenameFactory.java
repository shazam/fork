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
package com.shazam.fork.runtime;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;

public class ScreenRecorderFilenameFactory {
    private static final String FOLDER = "recordings";
    private static final String MP4 = ".mp4";

    static String remoteVideoForTest(String remoteFolder, TestIdentifier test) {
        return remoteFolder + "/" + videoFileName(test);
    }

    static String videosIn(String remoteFolder) {
        return remoteFolder + "/*" + MP4;
    }

    static String localVideoPathName(File output, Device device, TestIdentifier test) throws IOException {
        Path path = Paths.get(output.getAbsolutePath(), FOLDER, device.getSimpleName(), videoFileName(test));
        return createFileOnPath(path);
    }

    private static String createFileOnPath(Path path) throws IOException {
        createDirectories(path.getParent());
        Path file = createFile(path);
        return file.toFile().getAbsolutePath();
    }

    private static String videoFileName(TestIdentifier test) {
        return test.getClassName() + "-" + test.getTestName() + MP4;
    }
}
