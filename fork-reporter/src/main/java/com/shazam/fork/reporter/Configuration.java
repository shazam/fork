/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter;

import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;

public class Configuration {
    private final File input;
    private final File output;
    private final String title;
    private final String baseUrl;

    public Configuration(File input, File output, String title, String baseUrl) {
        this.input = input;
        this.output = output;
        this.title = title;
        this.baseUrl = baseUrl;
    }

    public File getOutput() {
        return output;
    }

    public File getInput() {
        return input;
    }

    public String getTitle() {
        return title;
    }

    public boolean shouldCreateLinks() {
        return !isNullOrEmpty(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
