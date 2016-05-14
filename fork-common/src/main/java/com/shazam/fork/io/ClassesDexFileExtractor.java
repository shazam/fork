/*
 * Copyright 2016 Shazam Entertainment Limited
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
import java.util.Collection;
import java.util.zip.*;

import static com.shazam.fork.io.Files.convertFileToDexFile;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;

public class ClassesDexFileExtractor implements DexFileExtractor {
    private static final String CLASSES_PREFIX = "classes";
    private static final String DEX_EXTENSION = ".dex";
    private final File outputDirectory;

    public ClassesDexFileExtractor(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Collection<DexFile> getDexFiles(File apkFile) {
        dumpDexFilesFromApk(apkFile, outputDirectory);
        File[] dexFiles = outputDirectory.listFiles((dir, name) -> name.startsWith("classes") && name.endsWith(".dex"));
        return asList(dexFiles)
                .stream()
                .map(convertFileToDexFile())
                .filter(f -> f != null)
                .collect(toList());
    }

    private void dumpDexFilesFromApk(File apkFile, File outputFolder) {
        ZipFile zip = null;
        InputStream classesDexInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            zip = new ZipFile(apkFile);
            int index = 1;
            String currentDex;
            while (true) {
                currentDex = CLASSES_PREFIX + (index > 1 ? index : "") + DEX_EXTENSION;
                ZipEntry classesDex = zip.getEntry(currentDex);
                if (classesDex != null) {
                    File dexFileDestination = new File(outputFolder, currentDex);
                    classesDexInputStream = zip.getInputStream(classesDex);
                    fileOutputStream = new FileOutputStream(dexFileDestination);
                    copyLarge(classesDexInputStream, fileOutputStream);
                    index++;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new DexFileExtractionException("Error when trying to scan " + apkFile.getAbsolutePath()
                    + " for test classes.", e);
        } finally {
            closeQuietly(classesDexInputStream);
            closeQuietly(fileOutputStream);
            closeQuietly(zip);
        }
    }
}
