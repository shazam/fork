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
package com.shazam.fork.geometry;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Writes the geometry command output to a file for diagnostics.
 */
public class GeometryCommandOutputLogger implements CommandOutputLogger {
    private final static String GEOMETRY_STRATEGY = "geometryStrategy";
    private final String command;
    private final File strategyDir;

    public GeometryCommandOutputLogger(File output, String command) {
        this.command = command;
        strategyDir = new File(output, GEOMETRY_STRATEGY);
    }

    @Override
    public void logCommandOutput(String deviceIdentifier, String commandOutput) throws IOException {
        strategyDir.mkdirs();
        File file = new File(strategyDir, deviceIdentifier + "." + command.replaceAll("\\W+", "-") + ".txt");
        FileUtils.writeStringToFile(file, commandOutput);
    }
}
