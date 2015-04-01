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
package com.shazam.fork.suite;

import com.shazam.fork.model.TestClass;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;

public class TestClassScanner {
    private static final String CLASSES_PREFIX = "classes";
    private static final String DEX_EXTENSION = ".dex";

    private final File instrumentationApkFile;
    private final File outputFolder;
    private final TestClassMatcher testClassMatcher;
    private final TestClassFactory testClassFactory;

    public TestClassScanner(File instrumentationApkFile,
                            File outputFolder,
                            TestClassMatcher testClassMatcher,
                            TestClassFactory testClassFactory) {
        this.instrumentationApkFile = instrumentationApkFile;
        this.outputFolder = outputFolder;
        this.testClassMatcher = testClassMatcher;
        this.testClassFactory = testClassFactory;
    }

    public List<TestClass> scanForTestClasses() {
        File[] instrumentationDexFiles = getDexFiles(instrumentationApkFile, outputFolder);
        return getTestClassesFrom(instrumentationDexFiles);
    }

    private File[] getDexFiles(File instrumentationApkFile, File dexFilesFolder) {
        dumpDexFilesFromApk(instrumentationApkFile, dexFilesFolder);
        return dexFilesFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("classes") && name.endsWith(".dex");
            }
        });
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(classesDexInputStream);
            closeQuietly(fileOutputStream);
            closeQuietly(zip);
        }
    }

    private List<TestClass> getTestClassesFrom(File[] dexFiles) {
        try {
            List<TestClass> testClasses = new ArrayList<>();
            for (File file : dexFiles) {
                DexFile dexFile = new DexFile(file);
                List<ClassDefItem> items = dexFile.ClassDefsSection.getItems();
                for (ClassDefItem classDefItem : items) {
                    String typeDescriptor = classDefItem.getClassType().getTypeDescriptor();
                    if (testClassMatcher.matchesPatterns(typeDescriptor)) {
                        TestClass testClass = testClassFactory.createTestFromDexClass(classDefItem);
                        testClasses.add(testClass);
                    }
                }
            }
            return testClasses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
