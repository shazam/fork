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

/**
 * Expresses pooling 'by smallest screen width' strategy.
 */
public class PoolBySmallestWidth implements PoolingStrategy {

    @Override
    public boolean canPool(Device device) {
        return device.getGeometry() != null;
    }

    @Override
    public int getParameter(Device device) {
        return device.getGeometry().getSwDp();
    }

    @Override
    public String getBaseName() {
        return "sw";
    }

    @Override
    public String help() {
        return "by smallest width, e.g.phablet=0,tablet=720";
    }
}
