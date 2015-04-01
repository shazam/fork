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

import java.util.Collection;

/**
 * Return the name of the pool for the device determined by the array of bounds and the PoolingStrategy
 */
public class ComputedPoolsConfiguration {

	private final Bounds bounds;
	private final ComputedPoolingStrategy computedPoolingStrategy;

	public ComputedPoolsConfiguration(Bounds bounds, ComputedPoolingStrategy computedPoolingStrategy) {
		this.bounds = bounds;
		this.computedPoolingStrategy = computedPoolingStrategy;
	}

	public String poolForDevice(Device device) {
		if (!computedPoolingStrategy.canPool(device)) {
			return null;
		}
		int small = computedPoolingStrategy.getParameter(device);
		int i = bounds.findEnclosingBoundIndex(small);
		return bounds.getName(i, computedPoolingStrategy);
	}

	public Collection<String> allPools() {
		return bounds.allNames(computedPoolingStrategy);
	}
}
