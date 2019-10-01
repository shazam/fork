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
package com.shazam.fork.pooling.geometry;

/**
 * Contract for logging a command output from a device.
 */
public interface CommandOutputLogger {

    /**
     * Log the output of a command that has run on a device with a given name.
     * @param deviceIdentifier the device identifier, e.g. the name
     * @param commandOutput the output of a command
     * @throws Exception in case an exception happened during the logging
     */
    void logCommandOutput(String deviceIdentifier, String commandOutput) throws Exception;
}
