/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.summary;

import com.google.gson.Gson;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class JsonSummarySerializer implements SummaryPrinter {

    private static final Logger logger = LoggerFactory.getLogger(JsonSummarySerializer.class);
    private final FileManager fileManager;
    private final Gson gson;

    public JsonSummarySerializer(FileManager fileManager, Gson gson) {
        this.fileManager = fileManager;
        this.gson = gson;
    }

    @Override
    public void print(boolean isSuccessful, Summary summary) {
        FileWriter writer = null;
        try {
            File summaryFile = fileManager.createSummaryFile();
            writer = new FileWriter(summaryFile);
            gson.toJson(summary, writer);
            writer.flush();
        } catch (IOException e) {
            logger.error("Could not serialize the summary.", e);
        } finally {
            closeQuietly(writer);
        }
    }
}
