/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.injector.device;

import com.google.common.collect.ImmutableList;
import com.shazam.fork.device.DeviceGeometryRetriever;
import com.shazam.fork.device.DisplayGeometryRetrievalStrategy;
import com.shazam.fork.pooling.geometry.RegexDisplayGeometryRetrievalStrategy;

import java.util.List;

import static com.shazam.fork.injector.pooling.CommandOutputLoggerInjector.commandOutputLogger;

class DeviceGeometryRetrieverInjector {

    /**
     * Nexus S, Samsumg GT-P5110.
     * Also (h\\d+)dp and (w\\d+)dp if you want them...
     */
    private static DisplayGeometryRetrievalStrategy swxxxdp() {
        return createRegexDisplayGeometryRetrievalStrategy("dumpsys window windows", "\\s(sw\\d+)dp\\s");
    }
    /**
     * Nexus 7 - sw600 with [800 x 1280] * 213 / 160 = [600, 880]dp
     */
    private static DisplayGeometryRetrievalStrategy baseDisplay() {
        return createRegexDisplayGeometryRetrievalStrategy("dumpsys display",
                "mBaseDisplayInfo=DisplayInfo\\{\"Built-in Screen\",.* largest app (\\d+) x (\\d+).*density (\\d+)");
    }

    /**
     * Changed for Samsumg GT-P9110 - XScale is Touch screen RAW scaling! For Nexus S, scale="" means 1.0
     */
    private static DisplayGeometryRetrievalStrategy gti91100() {
        return createRegexDisplayGeometryRetrievalStrategy("dumpsys window",
                "(?s)SurfaceWidth: (\\d+)px\\s*SurfaceHeight: (\\d+)px.*?XScale: ()");
    }

    private static DisplayGeometryRetrievalStrategy createRegexDisplayGeometryRetrievalStrategy(
            String command, String regex) {
        return new RegexDisplayGeometryRetrievalStrategy(command, commandOutputLogger(command), regex);
    }

    private static final List<DisplayGeometryRetrievalStrategy> STRATEGIES = ImmutableList.of(
            swxxxdp(), baseDisplay(), gti91100()
    );

    public static DeviceGeometryRetriever deviceGeometryReader() {
        return new DeviceGeometryRetriever(STRATEGIES);
    }

}
