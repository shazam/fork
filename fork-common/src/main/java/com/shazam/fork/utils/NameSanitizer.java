/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.utils;

import com.google.common.io.BaseEncoding;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NameSanitizer {
    private Pattern pattern;

    public NameSanitizer(boolean escapeUnderscores) {
        pattern = createMatchingPattern(escapeUnderscores);
    }

    @Nonnull
    public String sanitizeReportName(@Nonnull String reportName, boolean escapeUnderscores) {
        Matcher matcher = pattern.matcher(reportName);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            matcher.appendReplacement(buffer, "_" + encodeHexString(match.getBytes(UTF_8)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @Nonnull
    private Pattern createMatchingPattern(boolean escapeUnderscores) {
        String pattern = "[^a-zA-Z0-9-_]";
        if (escapeUnderscores) {
            pattern = "[^a-zA-Z0-9-]";
        }
        return Pattern.compile(pattern);
    }

    @Nonnull
    private String encodeHexString(byte[] input) {
        return BaseEncoding.base16().lowerCase().encode(input);
    }
}
