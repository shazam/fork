/*
 * Copyright 2018 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.pooling;

import com.shazam.fork.ManualPooling;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Devices;
import com.shazam.fork.model.Pool;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.shazam.fork.matcher.PoolMatcher.samePool;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Devices.Builder.devices;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class SerialBasedDevicePoolLoaderTest {
    private final Device device = aDevice()
            .withModel("aModel")
            .build();
    private final Device anotherDevice = aDevice()
            .withModel("anotherModel")
            .build();
    private final Device emulator = aDevice()
            .withModel("emulator")
            .build();
    private final Device anotherEmulator = aDevice()
            .withModel("anotherEmulator")
            .build();
    private final Devices devices = devices()
            .putDevice("serial1", device)
            .putDevice("serial2", anotherDevice)
            .putDevice("192.168.68.101:5555", emulator)
            .putDevice("192.168.68.102:5555", anotherEmulator)
            .build();

    @Test
    public void loadPoolsWhenUsingSpecificSerials() {
        Map<String, Collection<String>> groupings = newHashMap();
        groupings.put("VMs", asList("192.168.68.101:5555", "192.168.68.102:5555"));
        groupings.put("Real devices", asList("serial1", "serial2"));
        SerialBasedDevicePoolLoader poolLoader = new SerialBasedDevicePoolLoader(new ManualPooling(groupings));

        Collection<Pool> pools = poolLoader.loadPools(devices);

        assertThat(pools, containsInAnyOrder(
                samePool(
                        aDevicePool()
                                .withName("Real devices")
                                .addDevice(device)
                                .addDevice(anotherDevice)
                                .build()
                ),
                samePool(
                        aDevicePool()
                                .withName("VMs")
                                .addDevice(emulator)
                                .addDevice(anotherEmulator)
                                .build()
                )
        ));
    }

    @Test
    public void loadsPoolsWhenUsingRegexpToDescribeSerials() {
        Map<String, Collection<String>> groupings = newHashMap();
        groupings.put("VMs", singletonList(".*:5555"));
        groupings.put("Real devices", asList("serial1", "serial2"));
        SerialBasedDevicePoolLoader poolLoader = new SerialBasedDevicePoolLoader(new ManualPooling(groupings));

        Collection<Pool> pools = poolLoader.loadPools(devices);

        assertThat(pools, containsInAnyOrder(
                samePool(
                        aDevicePool()
                                .withName("Real devices")
                                .addDevice(device)
                                .addDevice(anotherDevice)
                                .build()
                ),
                samePool(
                        aDevicePool()
                                .withName("VMs")
                                .addDevice(emulator)
                                .addDevice(anotherEmulator)
                                .build()
                )
        ));
    }
}