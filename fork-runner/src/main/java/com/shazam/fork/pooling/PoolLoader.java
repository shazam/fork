/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.pooling;

import com.shazam.fork.Configuration;
import com.shazam.fork.PoolingStrategy;
import com.shazam.fork.device.DeviceLoader;
import com.shazam.fork.model.Devices;
import com.shazam.fork.model.Pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.lang.String.format;

public class PoolLoader {
    private static final Logger logger = LoggerFactory.getLogger(PoolLoader.class);
    private final DeviceLoader deviceLoader;
    private final Configuration configuration;

    public PoolLoader(DeviceLoader deviceLoader, Configuration configuration) {
        this.deviceLoader = deviceLoader;
        this.configuration = configuration;
    }

    public Collection<Pool> loadPools() throws NoDevicesForPoolException, NoPoolLoaderConfiguredException {
        Devices devices = deviceLoader.loadDevices();
        if (devices.getDevices().isEmpty()) {
            throw new NoDevicesForPoolException("No devices found.");
        }

        DevicePoolLoader devicePoolLoader = pickPoolLoader(configuration);
        logger.info("Picked {}", devicePoolLoader.getClass().getSimpleName());
        Collection<Pool> pools = devicePoolLoader.loadPools(devices);
        if (pools.isEmpty()) {
            throw new IllegalArgumentException("No pools were found with your configuration. Please review connected devices");
        }
        log(pools);
        for (Pool pool : pools) {
            if (pool.isEmpty()) {
                throw new NoDevicesForPoolException(format("Pool %s is empty", pool.getName()));
            }
        }

        return pools;
    }

    private void log(Collection<Pool> configuredPools) {
        logger.info("Number of device pools: " + configuredPools.size());
        for (Pool pool : configuredPools) {
            logger.debug(pool.toString());
        }
    }

    private DevicePoolLoader pickPoolLoader(Configuration configuration) throws NoPoolLoaderConfiguredException {
        PoolingStrategy poolingStrategy = configuration.getPoolingStrategy();

        if (poolingStrategy.manual != null) {
            return new SerialBasedDevicePoolLoader(poolingStrategy.manual);
        }

        if (poolingStrategy.splitTablets != null && poolingStrategy.splitTablets) {
            return new DefaultAndTabletDevicePoolLoader();
        }

        if (poolingStrategy.computed != null) {
            return new ComputedDevicePoolLoader(poolingStrategy.computed);
        }

        if (poolingStrategy.eachDevice != null && poolingStrategy.eachDevice) {
            return new EveryoneGetsAPoolLoader();
        }

        throw new NoPoolLoaderConfiguredException("Could not determine which how to load pools to use based on your configuration");
    }
}
