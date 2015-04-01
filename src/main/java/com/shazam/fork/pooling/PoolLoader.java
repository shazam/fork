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

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public class PoolLoader {
    private static final Logger logger = LoggerFactory.getLogger(PoolLoader.class);

    private final DeviceLoader deviceLoader;
    private final DevicePoolLoader devicePoolLoader;

    public PoolLoader(DeviceLoader deviceLoader, DevicePoolLoader devicePoolLoader) {
        this.deviceLoader = deviceLoader;
        this.devicePoolLoader = devicePoolLoader;
    }

    public Collection<Pool> loadPools() throws NoDevicesForPoolException {
        Devices devices = deviceLoader.loadDevices();
        if (devices.getDevices().isEmpty()) {
            logger.error("No devices found, returning empty pools");
            return emptyList();
        }

        Collection<Pool> pools = devicePoolLoader.loadPools(devices);
        validatePools(pools);
        return pools;
    }

    private void validatePools(Collection<Pool> pools) throws NoDevicesForPoolException {
        for (Pool pool : pools) {
            if (pool.isEmpty()) {
                throw new NoDevicesForPoolException(format("No connected devices in pool %s", pool.getName()));
            }
        }
    }

}
