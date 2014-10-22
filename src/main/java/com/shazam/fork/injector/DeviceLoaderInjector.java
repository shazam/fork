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
package com.shazam.fork.injector;

import com.shazam.fork.system.DeviceLoader;

import static com.shazam.fork.injector.AndroidDebugBridgeInjector.androidDebugBridge;
import static com.shazam.fork.injector.DeviceGeometryReaderInjector.deviceGeometryReader;
import static com.shazam.fork.injector.RuntimeConfigurationInjector.runtimeConfiguration;

public class DeviceLoaderInjector {

    public static DeviceLoader deviceLoader() {
        return new DeviceLoader(androidDebugBridge(), deviceGeometryReader(), runtimeConfiguration());
    }
}
