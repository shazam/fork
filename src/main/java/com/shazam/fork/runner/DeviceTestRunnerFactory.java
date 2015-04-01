/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.model.*;
import com.shazam.fork.listeners.SwimlaneConsoleLogger;
import com.shazam.fork.system.adb.Installer;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class DeviceTestRunnerFactory {

    private final Installer installer;
    private final SwimlaneConsoleLogger swimlaneConsoleLogger;
    private final TestRunFactory testRunFactory;

    public DeviceTestRunnerFactory(Installer installer,
                                   SwimlaneConsoleLogger swimlaneConsoleLogger,
                                   TestRunFactory testRunFactory) {
        this.installer = installer;
        this.swimlaneConsoleLogger = swimlaneConsoleLogger;
        this.testRunFactory = testRunFactory;
    }

    public Runnable createDeviceTestRunner(Pool pool,
                                           Queue<TestClass> testClassQueue,
                                           CountDownLatch deviceInPoolCountDownLatch,
                                           Device device) {
        return new DeviceTestRunner(
                installer,
                pool.getName(),
                device,
                testClassQueue,
                deviceInPoolCountDownLatch,
                swimlaneConsoleLogger,
                testRunFactory);
    }
}
