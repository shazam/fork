/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.collect.Lists;
import com.shazam.fork.model.Device;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Maps.newHashMap;

public class FailureAccumulatorImpl implements FailureAccumulator {

    private final Map<Device, Collection<TestIdentifier>> failures;

    public FailureAccumulatorImpl() {
        failures = newHashMap();
    }

    @Override
    public void testFailed(Device device, TestIdentifier failedTest) {
        ensureDevice(device);
        failures.get(device).add(failedTest);
    }

    private void ensureDevice(Device device) {
        if (!failures.containsKey(device)) {
            failures.put(device, Lists.<TestIdentifier>newArrayList());
        }
    }

    @Override
    public boolean isEmpty(){
        return failures.isEmpty();
    }

    @Override
    public Map<Device, Collection<TestIdentifier>> getFailures() {
        return copyOf(failures);
    }
}
