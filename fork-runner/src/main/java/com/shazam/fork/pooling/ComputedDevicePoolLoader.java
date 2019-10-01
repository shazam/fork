/*
 * Copyright 2019 Apple Inc.
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
package com.shazam.fork.pooling;

import com.shazam.fork.ComputedPooling;
import com.shazam.fork.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static java.util.stream.Collectors.toCollection;

/**
 * Allocate devices into pools specified by nominated strategy.
 */
public class ComputedDevicePoolLoader implements DevicePoolLoader {
    private static final Logger logger = LoggerFactory.getLogger(ComputedDevicePoolLoader.class);
    private final ComputedPoolsCategorizer computedPoolsCategorizer;

    public ComputedDevicePoolLoader(ComputedPooling computedPooling) {
        this.computedPoolsCategorizer = new ComputedPoolsCategorizer(computedPooling);
    }

    public Collection<Pool> loadPools(Devices devices) {
        Collection<Pool> pools = createComputedPools(devices);
        ensureAllPoolsAreRepresented(pools);
        return pools;
    }

    private Collection<Pool> createComputedPools(Devices devices) {
        Map<String, Pool.Builder> pools = new HashMap<>();
        for (Device device : devices.getDevices()) {
            String poolName = computedPoolsCategorizer.poolForDevice(device);
            if (poolName != null) {
                if (!pools.containsKey(poolName)) {
                    pools.put(poolName, aDevicePool().withName(poolName));
                }
                pools.get(poolName).addDevice(device);
                logger.debug("Adding {} to pool {}", device.getLongName(), poolName);
            } else {
                logger.warn("Could not infer pool for " + device.getLongName() + ". Not adding to any pools");
            }
        }

        return createDevicePools(pools);
    }

    private Collection<Pool> createDevicePools(Map<String, Pool.Builder> pools) {
        return pools.values()
                .stream()
                .map(Pool.Builder::build)
                .collect(toCollection(HashSet::new));
    }

    private void ensureAllPoolsAreRepresented(Collection<Pool> pools) {
        for (String poolName : computedPoolsCategorizer.allPools()) {
            boolean found = false;
            for (Pool pool : pools) {
                if (pool.getName().equals(poolName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Computed pools must all be represented, but '" + poolName + "' was empty: aborting");
            }
        }
    }
}
