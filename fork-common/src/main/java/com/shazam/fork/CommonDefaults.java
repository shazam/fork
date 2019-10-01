/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork;

public class CommonDefaults {

    private CommonDefaults() {
    }

    public static final String ANDROID_SDK = System.getenv("ANDROID_HOME");
    public static final String FORK = "fork-";
    public static final String JSON = "json";
    public static final String TEST_CLASS_REGEX = "^((?!Abstract).)*Test$";
    public static final String FORK_SUMMARY_FILENAME_FORMAT = FORK + "%s." + JSON;
    public static final String FORK_SUMMARY_FILENAME_REGEX = FORK + ".*\\." + JSON;
    public static final String BUILD_ID_TOKEN = "{BUILD_ID}";
}
