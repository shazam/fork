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
package com.shazam.fork.injector;

import com.shazam.fork.RuntimeConfiguration;

import static com.shazam.fork.RuntimeConfiguration.Builder.aRuntimeConfiguration;
import static com.shazam.fork.RuntimeConfigurationExtractor.*;
import static com.shazam.fork.injector.PoolingStrategyInjector.poolingStrategies;
import static com.shazam.fork.injector.ComputedPoolsConfigurationFactoryInjector.computedPoolsConfigurationFactory;

public class RuntimeConfigurationInjector {

    private static final RuntimeConfiguration RUNTIME_CONFIGURATION = aRuntimeConfiguration()
            .withFilterPattern(extractFilterPattern())
            .whichUsesTabletFlag(extractTabletFlag())
            .withSerialBasedPools(extractSerialBasedPools())
            .withcomputedPoolsConfiguration(extractComputedPoolsConfiguration(poolingStrategies(), computedPoolsConfigurationFactory()))
            .whichCreatesPoolForEachDevice(extractPoolPerDeviceFlag())
            .withExcludedSerials(extractExcludedSerials())
            .withTitle(extractTitle())
            .withSubtitle(extractSubtitle())
            .withTestSize(extractTestSize())
            .build();

    public static RuntimeConfiguration runtimeConfiguration() {
        return RUNTIME_CONFIGURATION;
    }
}
