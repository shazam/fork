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

import com.shazam.fork.model.Permission;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

import static com.shazam.fork.model.Permission.Builder.permission;

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

            List<Permission> permissions = new ArrayList<>();
            while (eventType != AXMLParser.END_DOCUMENT) {
                if (eventType == AXMLParser.START_TAG) {
                    String parserName = parser.getName();
                    boolean isPermission =  parserName.startsWith("uses-permission");
                    if (isPermission) {
                        parsePermission(parser, permissions);
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

    private static void parsePermission(AXMLParser parser, List<Permission> permissions) {
        Permission.Builder builder = permission();

        int minApi = "uses-permission-sdk-23".equals(parser.getName()) ? 23 : 1;
        builder.withMinSdkVersion(minApi);

        //we need the attribs  "name" and "maxSdkVersion". Example: <uses-permission android:name="android.permission.XYZ" android:maxSdkVersion="24"...
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if ("name".equals(attributeName)) {
                builder.withPermissionName(parser.getAttributeValueString(i));
            }
            if ("maxSdkVersion".equals(attributeName)) {
                int maxSdkVersion = parser.getAttributeValue(i);
                builder.withMaxSdkVersion(maxSdkVersion);
            }
        }
        permissions.add(builder.build());
    }


}
