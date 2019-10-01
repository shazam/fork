/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.system.axmlparser;

import com.shazam.axmlparser.AXMLParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Parses key information from an instrumentation APK's manifest.
 */
public class InstrumentationInfoFactory {

    private InstrumentationInfoFactory() {
    }

    /**
     * @param apkTestFile the instrumentation APK
     * @return the instrumentation info instance
     */
    @Nonnull
    public static InstrumentationInfo parseFromFile(File apkTestFile) {
        InputStream is = null;
        try {
            ZipFile zip = new ZipFile(apkTestFile);
            ZipEntry entry = zip.getEntry("AndroidManifest.xml");
            is = zip.getInputStream(entry);

            AXMLParser parser = new AXMLParser(is);
            int eventType = parser.getType();

            String appPackage = null;
            String testPackage = null;
            String testRunnerClass = null;
            while (eventType != AXMLParser.END_DOCUMENT) {
                if (eventType == AXMLParser.START_TAG) {
                    String parserName = parser.getName();
                    boolean isManifest = "manifest".equals(parserName);
                    boolean isInstrumentation = "instrumentation".equals(parserName);
                    if (isManifest || isInstrumentation) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String parserAttributeName = parser.getAttributeName(i);
                            if (isManifest && "package".equals(parserAttributeName)) {
                                testPackage = parser.getAttributeValueString(i);
                            } else if (isInstrumentation && "targetPackage".equals(parserAttributeName)) {
                                appPackage = parser.getAttributeValueString(i);
                            } else if (isInstrumentation && "name".equals(parserAttributeName)) {
                                testRunnerClass = parser.getAttributeValueString(i);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
            checkNotNull(testPackage, "Could not find test application package.");
            checkNotNull(appPackage, "Could not find application package.");
            checkNotNull(testRunnerClass, "Could not find test runner class.");

            // Support relative declaration of instrumentation test runner.
            if (testRunnerClass.startsWith(".")) {
                testRunnerClass = testPackage + testRunnerClass;
            } else if (!testRunnerClass.contains(".")) {
                testRunnerClass = testPackage + "." + testRunnerClass;
            }

            return new InstrumentationInfo(appPackage, testPackage, testRunnerClass);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse test app AndroidManifest.xml.", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
