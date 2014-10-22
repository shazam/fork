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

import com.shazam.fork.model.DevicePool;
import com.shazam.fork.model.Devices;

import java.util.Collection;

/**
 * Loads pools of devices based on their characteristics.
 */
public interface DevicePoolLoader {

	/**
	 * Try to load pools according to your interpretation of the system properties.
     * @param devices the devices that this pool loader can apply to
	 * @return the pools loaded from this loader
	 */
	Collection<DevicePool> loadPools(Devices devices);
}
