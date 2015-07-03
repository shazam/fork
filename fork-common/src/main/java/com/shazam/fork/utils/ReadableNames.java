/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.utils;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public class ReadableNames {

    public static String readablePoolName(String poolName) {
        return capitalize(poolName);
    }

    public static String readableTitle(String title) {
        return capitalize(title);
    }

    private static String capitalize(String name) {
        return capitalizeFully(name.replaceAll("[\\W]|_", " "));
    }

    public static String readableClassName(String testClass) {
        final int lastIndexOfDot = testClass.lastIndexOf('.');
        if (lastIndexOfDot != -1) {
            testClass = testClass.substring(lastIndexOfDot+1);
        }
        return testClass;
    }

    public static String readableTestMethodName(String testMethod) {
        testMethod = testMethod
                .replaceFirst("test", "")
                .replaceAll("_", ", ")
                .replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2")
                .replaceAll("(\\p{Lu})(\\p{Lu})","$1 $2");
        return capitalizeFully(testMethod);
    }
}
