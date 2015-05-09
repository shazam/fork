/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork;

import java.util.regex.Pattern;

class Defaults {
    public static final int TEST_OUTPUT_TIMEOUT_MILLIS = 60 * 1000;
    public static final Pattern TEST_CLASS_PATTERN = Pattern.compile("^((?!Abstract).)*Test$");
    public static final String TEST_OUTPUT = "fork-output";
    public static final String ANDROID_SDK = System.getenv("ANDROID_HOME");
}
