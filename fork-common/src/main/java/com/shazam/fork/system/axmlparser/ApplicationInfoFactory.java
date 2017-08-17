/*
 * Copyright 2017 Shazam Entertainment Limited
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Parses key information from an instrumentation APK's manifest.
 */
public class ApplicationInfoFactory {

    private ApplicationInfoFactory() {
    }

    /**
     * @param apkTestFile the instrumentation APK
     * @return the instrumentation info instance
     */
    @Nonnull
    public static ApplicationInfo parseFromFile(File apkTestFile) {
        InputStream is = null;
        try {
            ZipFile zip = new ZipFile(apkTestFile);
            ZipEntry entry = zip.getEntry("AndroidManifest.xml");
            is = zip.getInputStream(entry);

            AXMLParser parser = new AXMLParser(is);
            int eventType = parser.getType();

            List<String> permissions = new ArrayList<>();
            while (eventType != AXMLParser.END_DOCUMENT) {
                if (eventType == AXMLParser.START_TAG) {
                    String parserName = parser.getName();
                    boolean isPermission = "uses-permission".equals(parserName);
                    if(isPermission) {
                        //we need the attrib "name". Example: <uses-permission android:name="android.permission.XYZ"...
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);
                            if ("name".equals(attributeName)) {
                                permissions.add(parser.getAttributeValueString(i));
                            }
                        }
                    }
                }
                eventType = parser.next();
            }

            return new ApplicationInfo(permissions);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse test app AndroidManifest.xml.", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
