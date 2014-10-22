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
import com.shazam.fork.runtime.LogCatFilenameFactory;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.shazam.fork.runtime.LogCatFilenameFactory.createLogCatFilenamePrefix;

public class JsonLogCatRetriever implements LogCatRetriever {
    private final Gson gson;
    private final File output;

    public JsonLogCatRetriever(File output, Gson gson) {
        this.output = output;
        this.gson = gson;
    }

    @Override
    public List<LogCatMessage> retrieveLogCat(String poolName, String serial, TestIdentifier testIdentifier) {
        String filenamePrefix = createLogCatFilenamePrefix(poolName, serial, testIdentifier);
        final PrefixFileFilter prefixFileFilter = new PrefixFileFilter(filenamePrefix);
        SuffixFileFilter suffixFileFilter = new SuffixFileFilter(LogCatFilenameFactory.JSON);
        final AndFileFilter filter = new AndFileFilter(prefixFileFilter, suffixFileFilter);
        File[] files = output.listFiles((FileFilter) filter);
        if (files.length == 0) {
            return new ArrayList<>();
        }
        File logcatJsonFile = files[0];

        try {
            FileReader fileReader = new FileReader(logcatJsonFile);
            return gson.fromJson(fileReader, new TypeToken<List<LogCatMessage>>() {}.getType());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}