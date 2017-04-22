/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.injector.runner;

import com.shazam.fork.runner.DeviceTestRunnerFactory;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;
import static com.shazam.fork.injector.system.FileManagerInjector.fileManager;
import static com.shazam.fork.injector.system.InstallerInjector.installer;
import static com.shazam.fork.injector.runner.TestRunFactoryInjector.testRunFactory;

public class DeviceTestRunnerFactoryInjector {
    private DeviceTestRunnerFactoryInjector() {}

    public static DeviceTestRunnerFactory deviceTestRunnerFactory() {
        return new DeviceTestRunnerFactory(configuration(), fileManager(), installer(), testRunFactory());
    }
}
