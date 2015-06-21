/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.pooling;

import com.shazam.fork.model.Devices;
import com.shazam.fork.model.Pool;

import java.util.Collection;

import static java.lang.String.format;

public class PoolLoader {
    private final DeviceLoader deviceLoader;
    private final DevicePoolLoader devicePoolLoader;

    public PoolLoader(DeviceLoader deviceLoader, DevicePoolLoader devicePoolLoader) {
        this.deviceLoader = deviceLoader;
        this.devicePoolLoader = devicePoolLoader;
    }

    public Collection<Pool> loadPools() throws NoDevicesForPoolException {
        Devices devices = deviceLoader.loadDevices();
        if (devices.getDevices().isEmpty()) {
            throw new NoDevicesForPoolException("No devices found.");
        }

        Collection<Pool> pools = devicePoolLoader.loadPools(devices);
        for (Pool pool : pools) {
            if (pool.isEmpty()) {
                throw new NoDevicesForPoolException(format("Pool %s is empty", pool.getName()));
            }
        }

        return pools;
    }

}
