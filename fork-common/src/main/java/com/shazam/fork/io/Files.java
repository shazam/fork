/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.io;

import org.jf.dexlib.DexFile;

import java.io.*;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;

public class Files {

    private Files() {}

    public static void copyResource(String fromDir, String assetName, File toDir) {
        InputStream resourceAsStream = Files.class.getResourceAsStream(fromDir + assetName);
        File assetFile = new File(toDir, assetName);
        try {
            copyInputStreamToFile(resourceAsStream, assetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(resourceAsStream);
        }
    }

    @Nonnull
    public static Function<File, DexFile> convertFileToDexFile() {
        return new Function<File, DexFile>() {
            @Nullable
            @Override
            public DexFile apply(File f) {
                try {
                    return new DexFile(f);
                } catch (IOException e) {
                    return null;
                }
            }
        };
    }
}
