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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SerialBasedPools {
    private final Map<String, Collection<String>> serialBasedPools;

    private SerialBasedPools(Builder builder) {
        this.serialBasedPools = builder.serialBasedPools;
    }

    public static class Builder {
        private final Map<String, Collection<String>> serialBasedPools = new HashMap<>();

        public static Builder serialBasedPools() {
            return new Builder();
        }

        public Builder withSerialBasedPool(String poolName, Collection<String> serials) {
            serialBasedPools.put(poolName, serials);
            return this;
        }

        public SerialBasedPools build() {
            return new SerialBasedPools(this);
        }
    }

    public Set<Map.Entry<String, Collection<String>>> getSerialBasedPools() {
        return serialBasedPools.entrySet();
    }
}
