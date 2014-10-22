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
package com.shazam.fork.system;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.geometry.DeviceGeometryRetriever;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Devices.Builder.devices;

/**
 * Turns a serial number or an IDevice reference to a Device.
 */
public class DeviceLoader {
    private final AndroidDebugBridge adb;
    private final DeviceGeometryRetriever deviceGeometryRetriever;
    private final Collection<String> excludedSerials;

    public DeviceLoader(AndroidDebugBridge adb, DeviceGeometryRetriever deviceGeometryRetriever,
                        RuntimeConfiguration runtimeConfiguration) {
        this.adb = adb;
        this.deviceGeometryRetriever = deviceGeometryRetriever;
        this.excludedSerials = runtimeConfiguration.getExcludedSerials();
    }

    /**
     * Retrieve all connected and non-excluded devices.
     *
     * @return the connected devices
     */
    public Devices loadDevices() {
        Devices.Builder devicesBuilder = devices();
        List<IDevice> iDevices = loadAllDevices();
        for (IDevice iDevice : iDevices) {
            devicesBuilder.putDevice(iDevice.getSerialNumber(), loadDeviceCharacteristics(iDevice));
        }

        return devicesBuilder.build();
    }

    /**
     * Retrieve all connected devices which survive the FORK_EXCLUDED_SERIAL filter.
     *
     * @return a list of connected devices
     */
    private List<IDevice> loadAllDevices() {
        List<IDevice> devices = new ArrayList<>();
        for (IDevice device : adb.getDevices()) {
            String serialNumber = device.getSerialNumber();
            if (!excludedSerials.contains(serialNumber)) {
                devices.add(device);
            }
        }
        return devices;
    }

    private Device loadDeviceCharacteristics(IDevice device) {
        return aDevice()
                .withSerial(device.getSerialNumber())
                .withManufacturer(device.getProperty("ro.product.manufacturer"))
                .withModel(device.getProperty("ro.product.model"))
                .withApiLevel(device.getProperty("ro.build.version.sdk"))
                .withDeviceInterface(device)
                .withTabletCharacteristic(device.getProperty("ro.build.characteristics"))
                .withDisplayGeometry(deviceGeometryRetriever.detectGeometry(device)).build();
    }
}
