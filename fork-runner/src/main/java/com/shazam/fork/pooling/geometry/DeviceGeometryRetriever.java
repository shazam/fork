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
package com.shazam.fork.pooling.geometry;

import com.android.ddmlib.IDevice;
import com.shazam.fork.model.DisplayGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Use various strategies to determine geometry of a device.
 */
public class DeviceGeometryRetriever {
    private final static Logger logger = LoggerFactory.getLogger(DeviceGeometryRetriever.class);
    private final List<DisplayGeometryRetrievalStrategy> strategies;

    public DeviceGeometryRetriever(List<DisplayGeometryRetrievalStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Detect the geometry of a device after applying a number of strategies.
     * @param device the connected device
     * @return the resolved geometry or null
     */
    //TODO: throw exception instead of trying to handle null geometry.
	public DisplayGeometry detectGeometry(IDevice device) {
		for (DisplayGeometryRetrievalStrategy strategy : strategies) {
			DisplayGeometry geometry = strategy.retrieveGeometry(device);
			if (geometry != null) {
				return geometry;
			}
		}
        logger.warn("No geometry found for {} ({})", device.getName(), device.getSerialNumber());
		return null;
	}
}
