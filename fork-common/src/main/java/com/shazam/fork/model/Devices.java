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
package com.shazam.fork.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

public class Devices {
    private final Map<String, Device> deviceMap;

    private Devices(Builder builder) {
        this.deviceMap = builder.deviceMap;
    }

    public static class Builder {
        private final Map<String, Device> deviceMap = new HashMap<>();

        public static Builder devices() {
            return new Builder();
        }

        public Builder putDevice(String serial, Device device) {
            deviceMap.put(serial, device);
            return this;
        }

        public Devices build() {
            return new Devices(this);
        }
    }

    @Nonnull
    public Collection<Device> getDevices() {
        return deviceMap.values();
    }

    /**
     * Find devices by the given pattern. If possible, this method will try to match serial numbers by a regexp, if
     * the expression is invalid then it will fallback to a default algorithm that matches 2 strings.
     *
     * @param patternToMatch the pattern to use to find devices
     * @return a collection of found devices, or empty if none found
     */
    @Nonnull
    public Collection<Device> findDevices(String patternToMatch) {
        Pattern pattern = tryCompilePattern(patternToMatch);
        if (pattern != null) {
            return findDevicesByPattern(pattern);
        } else {
            return findDeviceBySerial(patternToMatch);
        }
    }

    @Nullable
    private static Pattern tryCompilePattern(String regexp) {
        Pattern pattern = null;
        try {
            pattern = compile(regexp);
        } catch (PatternSyntaxException ignored) {
        }
        return pattern;
    }

    @Nonnull
    private Collection<Device> findDevicesByPattern(Pattern pattern) {
        return deviceMap.entrySet().stream()
                .filter(entry -> matchesPattern(pattern, entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    private static boolean matchesPattern(Pattern pattern, String serial) {
        return pattern.matcher(serial).matches();
    }

    @Nonnull
    private Collection<Device> findDeviceBySerial(String serial) {
        return deviceMap.entrySet().stream()
                .filter(entry -> serial.equals(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(Collections::singletonList)
                .orElse(emptyList());
    }
}
