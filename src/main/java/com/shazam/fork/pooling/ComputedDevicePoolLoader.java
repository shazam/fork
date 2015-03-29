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
package com.shazam.fork.pooling;

import com.shazam.fork.model.Device;
import com.shazam.fork.model.DevicePool;
import com.shazam.fork.model.Devices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.shazam.fork.model.DevicePool.Builder.aDevicePool;

/**
 * Allocate devices into pools specified by nominated strategy:
 * <dl><dt>-Dfork.computed.sw=520,720</dt><dd>smallest width pools: 0-519, 520-719, 720-up</dd></dl>
 * <dl><dt>-Dfork.computed.sw=phone=0,tablet=720</dt><dd>named pools: phone 0-719 tablet 720-up</dd></dl>
 * <dl><dt>-Dfork.computed.api=15</dt><dd>api pools: 0-14, 15-up</dd></dl>
 */
public class ComputedDevicePoolLoader implements DevicePoolLoader {
    private static final Logger logger = LoggerFactory.getLogger(ComputedDevicePoolLoader.class);
    private final ComputedPoolsConfiguration computedPoolsSelector;

	public ComputedDevicePoolLoader(ComputedPoolsConfiguration computedPoolsSelector) {
        this.computedPoolsSelector = computedPoolsSelector;
	}

	public Collection<DevicePool> loadPools(Devices devices) {
        Collection<DevicePool> pools = createComputedPools(devices);
        ensureAllPoolsAreRepresented(pools);
        return pools;
    }

    private Collection<DevicePool> createComputedPools(Devices devices) {
        Map<String, DevicePool.Builder> pools = new HashMap<>();
        for (Device device : devices.getDevices()) {
            String poolName = computedPoolsSelector.poolForDevice(device);
            if (poolName != null) {
                if (!pools.containsKey(poolName)) {
                    pools.put(poolName, aDevicePool().withName(poolName));
                }
                pools.get(poolName).addDevice(device);
                logger.debug("Adding {} to pool {}", device.getLongName(), poolName);
            }
            else {
                logger.warn("Could not infer pool for " + device.getLongName());
            }
        }

        return createDevicePools(pools);
    }

    private Collection<DevicePool> createDevicePools(Map<String, DevicePool.Builder> pools) {
        Collection<DevicePool> builtPools = new HashSet<>();
        for (DevicePool.Builder builder : pools.values()) {
            builtPools.add(builder.build());
        }
        return builtPools;
    }

    private void ensureAllPoolsAreRepresented(Collection<DevicePool> devicePools) {
        for (String poolName : computedPoolsSelector.allPools()) {
            boolean found = false;
            for (DevicePool devicePool : devicePools) {
                if (devicePool.getName().equals(poolName)) {
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
