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
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.Devices;

import java.util.ArrayList;
import java.util.Collection;

import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static java.util.Map.Entry;

/**
 * Load pools specified by -Dfork.pool.NAME=Serial_1,Serial_2
 */
public class SerialBasedDevicePoolLoader implements DevicePoolLoader {
    private final SerialBasedPools serialBasedPools;

    public SerialBasedDevicePoolLoader(SerialBasedPools serialBasedPools) {
        this.serialBasedPools = serialBasedPools;
    }

	public Collection<Pool> loadPools(Devices devices) {
		Collection<Pool> pools = new ArrayList<>();

        for (Entry<String, Collection<String>> pool : serialBasedPools.getSerialBasedPools()) {
            Pool.Builder poolBuilder = aDevicePool().withName(pool.getKey());
            for (String serial : pool.getValue()) {
                Device device = devices.getDevice(serial);
                if (device != null) {
                    poolBuilder.addDevice(device);
                }
            }
            pools.add(poolBuilder.build());
        }

		return pools;
	}
}
