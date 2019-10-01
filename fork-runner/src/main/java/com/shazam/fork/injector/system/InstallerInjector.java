/*
 * Copyright 2019 Apple Inc.
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
package com.shazam.fork.injector.system;

import com.shazam.fork.Configuration;
import com.shazam.fork.system.adb.Installer;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class InstallerInjector {

    private InstallerInjector() {}

    public static Installer installer() {
        Configuration configuration = configuration();
        String applicationPackage = configuration.getApplicationPackage();
        String instrumentationPackage = configuration.getInstrumentationPackage();

        return new Installer(applicationPackage, instrumentationPackage, configuration.getApplicationApk(),
                configuration.getInstrumentationApk(), configuration.isAutoGrantingPermissions());
    }
}
