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
public class SmallestComputedPoolsSelector implements ComputedPoolsSelector {

	private final Bounds bounds;
	private final PoolingStrategy poolingStrategy;

	public SmallestComputedPoolsSelector(Bounds bounds, PoolingStrategy poolingStrategy) {
		this.bounds = bounds;
		this.poolingStrategy = poolingStrategy;
	}

	@Override
	public String poolForDevice(Device device) {
		if (!poolingStrategy.canPool(device)) {
			return null;
		}
		int small = poolingStrategy.getParameter(device);
		int i = bounds.findEnclosingBoundIndex(small);
		return bounds.getName(i, poolingStrategy);
	}

	@Override
	public Collection<String> allPools() {
		return bounds.allNames(poolingStrategy);
	}
}
