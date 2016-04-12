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
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.XmlTestRunListener;
import com.shazam.fork.model.*;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;

import java.io.File;

import static com.shazam.fork.system.io.FileType.TEST;

public class ForkXmlTestRunListener extends XmlTestRunListener {
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;
    private final TestClass testClass;

    public ForkXmlTestRunListener(FileManager fileManager, Pool pool, Device device, TestClass testClass) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
        this.testClass = testClass;
    }

    @Override
    protected File getResultFile(File reportDir) {
        return fileManager.createFile(pool, device, testClass, TEST);
    }
}
