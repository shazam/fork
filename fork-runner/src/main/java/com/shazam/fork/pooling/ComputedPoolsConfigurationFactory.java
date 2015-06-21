/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.pooling;

import java.util.*;

/**
 * Based on user input, creates a configuration to be used when selecting pools automatically.
 */
public class ComputedPoolsConfigurationFactory {

    public ComputedPoolsConfiguration createConfiguration(String parameterPoolComputed,
                                                          Collection<String> keys,
                                                          Properties properties,
                                                          Collection<ComputedPoolingStrategy> poolingStrategies) {

        ComputedPoolsConfiguration configuration = null;
        for (String key : keys) {
            if (configuration != null) {
                throw new RuntimeException("Only one computed pool supported at a time");
            }

            Bounds bounds = getBounds(properties, key);
            for (ComputedPoolingStrategy computedPoolingStrategy : poolingStrategies) {
                if (key.equals(parameterPoolComputed + computedPoolingStrategy.getBaseName())) {
                    configuration = new ComputedPoolsConfiguration(bounds, computedPoolingStrategy);
                }
            }

            if (configuration == null) {
                throw new RuntimeException("Unrecognised computed pool: " + key);
            }
        }

        return configuration;
    }

    private Bounds getBounds(Properties properties, String key) {
        String[] boundaries = ((String) properties.get(key)).split(",");
        Bound[] boundArray = new Bound[boundaries.length];
        for (int i = 0; i < boundaries.length; ++i) {
            String[] bits = boundaries[i].split("=");
            boolean isNamed = bits.length > 1;
            String name = isNamed ? bits[0] : null;
            int lower = Integer.parseInt(isNamed ? bits[1] : bits[0]);
            boundArray[i] = new Bound(lower, name);
        }
        Arrays.sort(boundArray, new Comparator<Bound>() {

            @Override
            public int compare(Bound o1, Bound o2) {
                return o1.getLower() - o2.getLower();
            }
        });
        return new Bounds(boundArray);
    }
}
