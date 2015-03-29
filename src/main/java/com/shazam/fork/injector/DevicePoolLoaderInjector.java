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

import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.pooling.*;

import java.util.ArrayList;
import java.util.Collection;

import static com.shazam.fork.injector.RuntimeConfigurationInjector.runtimeConfiguration;

public class DevicePoolLoaderInjector {

    public static DevicePoolLoader devicePoolLoader() {
        return new CompositeDevicePoolLoader(devicePoolLoaders(runtimeConfiguration()));
    }

    private static Collection<DevicePoolLoader> devicePoolLoaders(RuntimeConfiguration runtimeConfiguration) {
        Collection<DevicePoolLoader> devicePoolLoaders = new ArrayList<>();
        SerialBasedPools serialBasedPools = runtimeConfiguration.getSerialBasedPools();
        if (serialBasedPools != null && !serialBasedPools.getSerialBasedPools().isEmpty()) {
            devicePoolLoaders.add(new SerialBasedDevicePoolLoader(serialBasedPools));
        }

        if (runtimeConfiguration.getComputedPoolsConfiguration() != null) {
            devicePoolLoaders.add(new ComputedDevicePoolLoader(runtimeConfiguration.getComputedPoolsConfiguration()));
        }

        if (runtimeConfiguration.isUsingTabletFlag()) {
            devicePoolLoaders.add(new DefaultAndTabletDevicePoolLoader(true));
        }

        devicePoolLoaders.add(new EveryoneGetsAPoolLoader(runtimeConfiguration.isCreatingPoolForEachDevice()));
        return devicePoolLoaders;
    }
}
