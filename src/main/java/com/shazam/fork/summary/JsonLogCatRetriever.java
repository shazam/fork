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
package com.shazam.fork.summary;

import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shazam.fork.io.FileType;
import com.shazam.fork.io.FilenameCreator;

import java.io.*;
import java.util.List;

import static java.lang.String.format;

public class JsonLogCatRetriever implements LogCatRetriever {
    private final Gson gson;
    private final FilenameCreator filenameCreator;

    public JsonLogCatRetriever(Gson gson, FilenameCreator filenameCreator) {
        this.gson = gson;
        this.filenameCreator = filenameCreator;
    }

    @Override
    public List<LogCatMessage> retrieveLogCat(String poolName, String serial, TestIdentifier testIdentifier) {
        File logcatJsonFile = filenameCreator.getFile(FileType.JSON_LOG, poolName, serial, testIdentifier);
        try {
            FileReader fileReader = new FileReader(logcatJsonFile);
            return gson.fromJson(fileReader, new TypeToken<List<LogCatMessage>>() {}.getType());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
