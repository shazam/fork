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

import com.shazam.fork.model.*;

import java.util.ArrayList;
import java.util.Collection;

import static com.shazam.fork.model.Pool.Builder.aDevicePool;

/**
 * Create tablets/other pool based on self-reported ro.build.characteristics = tablet
 */
public class DefaultAndTabletDevicePoolLoader implements DevicePoolLoader {

	private static final String DEFAULT_POOL_NAME = "default_pool";
	private static final String TABLETS = "tablets";

	public DefaultAndTabletDevicePoolLoader() {
    }

	public Collection<Pool> loadPools(Devices devices) {
        Collection<Pool> pools = new ArrayList<>();
        Pool.Builder defaultPoolBuilder = aDevicePool().withName(DEFAULT_POOL_NAME);
        Pool.Builder tabletPoolBuilder = aDevicePool().withName(TABLETS);

        for (Device device : devices.getDevices()) {
            if (device.isTablet()) {
                tabletPoolBuilder.addDevice(device);
            } else {
                defaultPoolBuilder.addDevice(device);
            }
        }
        defaultPoolBuilder.addIfNotEmpty(pools);
        tabletPoolBuilder.addIfNotEmpty(pools);
		return pools;
	}
}
