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

import com.shazam.fork.ComputedPooling;
import com.shazam.fork.DeviceCharacteristicReader;
import com.shazam.fork.model.Device;

import java.util.*;

import javax.annotation.Nullable;

import static java.util.stream.Collectors.toList;

/**
 * Return the name of the pool for the device determined by the array of bounds and the PoolingStrategy
 */
public class ComputedPoolsCategorizer {
	private final List<Bound> bounds;
	private final DeviceCharacteristicReader deviceCharacteristicReader;

	public ComputedPoolsCategorizer(ComputedPooling computedPooling) {
		this.bounds = createBounds(computedPooling);
		this.deviceCharacteristicReader = computedPooling.characteristic;
	}

	@Nullable
    public String poolForDevice(Device device) {
		if (!deviceCharacteristicReader.canPool(device)) {
			return null;
		}
		int deviceParameter = deviceCharacteristicReader.getParameter(device);
        int enclosingBoundIndex = findEnclosingBoundIndex(deviceParameter);
        return getName(enclosingBoundIndex, deviceCharacteristicReader);
    }

	public Collection<String> allPools() {
		List<String> list = new ArrayList<>();
		for (int i = 1; i <= bounds.size(); ++i) {
			list.add(getName(i, deviceCharacteristicReader));
		}
		return list;
	}

	private List<Bound> createBounds(ComputedPooling computedPooling) {
		List<Bound> boundList = computedPooling.groups
				.entrySet()
				.stream()
				.map(entry -> new Bound(entry.getKey(), entry.getValue()))
				.collect(toList());

		boundList.sort((o1, o2) -> o1.getLower() - o2.getLower());
		return boundList;
	}

    private int findEnclosingBoundIndex(int parameter) {
		int i = 0;
		while (i < bounds.size() && parameter >= bounds.get(i).getLower()) {
			++i;
		}
		return i;
	}

	private String getName(int idx, DeviceCharacteristicReader computedPoolingStrategy) {
		Bound lower = (idx == 0 ? new Bound(null, 0) : bounds.get(idx - 1));
		return lower.getName()
                + computedPoolingStrategy.getBaseName()
                + lower.getLower()
                + (idx < bounds.size() ? "-" + (bounds.get(idx).getLower() - 1) : "-up");
	}

}
