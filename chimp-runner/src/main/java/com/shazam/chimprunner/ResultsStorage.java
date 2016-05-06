/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

import com.shazam.fork.model.TestCaseEvent;

import java.io.*;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

public class ResultsStorage {
    private final File output;

    public ResultsStorage(File output) {
        this.output = output;
    }

    public void storeResults(Map<TestCaseEvent, Double> results) throws ResultStorageException {
        try {
            FileWriter writer = new FileWriter(new File(output, "timings.csv"));
            CSVWriter csvWriter = new CSVWriter(writer);
            write(results, csvWriter);
            csvWriter.close();
        } catch (IOException e) {
            throw new ResultStorageException("Failed to write results", e);
        }
    }

    private void write(Map<TestCaseEvent, Double> results, CSVWriter csvWriter) {
        int size = results.size();
        String[] keys = new String[size];
        String[] values = new String[size];
        int i = 0;

        for (Map.Entry<TestCaseEvent, Double> entry : results.entrySet()) {
            TestCaseEvent key = entry.getKey();
            keys[i] = key.getTestClass() + "_" + key.getTestMethod();
            values[i] = String.valueOf(entry.getValue());
            i++;
        }

        csvWriter.writeNext(keys);
        csvWriter.writeNext(values);
    }
}
