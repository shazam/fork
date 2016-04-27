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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public Device getDevice(String serial) {
        return deviceMap.get(serial);
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }
}
