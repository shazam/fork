/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter;

import com.google.gson.Gson;
import com.shazam.fork.reporter.model.Execution;
import com.shazam.fork.reporter.model.Executions;
import com.shazam.fork.summary.Summary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.function.Function;

import static com.shazam.fork.CommonDefaults.FORK;
import static com.shazam.fork.reporter.model.Execution.Builder.execution;
import static com.shazam.fork.reporter.model.Executions.Builder.executions;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public class ExecutionReader {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionReader.class);

    private final FileManager fileManager;
    private final Gson gson;

    public ExecutionReader(FileManager fileManager, Gson gson) {
        this.fileManager = fileManager;
        this.gson = gson;
    }

    public Executions readExecutions() {
        List<File> individualSummaries = fileManager.getIndividualSummaries();
        List<Execution> executions = individualSummaries.stream()
                .map(getFileSummaryFunction())
                .collect(toList());

        return executions().withExecutions(executions).build();
    }

    private Function<File, Execution> getFileSummaryFunction() {
        return file -> {
            try {
                String timestampString = getBaseName(file.getName()).replaceAll(FORK, "");
                logger.debug("Reading summary file: {}", file.toString());
                Reader reader = new FileReader(file);
                Summary summary = gson.fromJson(reader, Summary.class);
                return execution()
                        .withTimestamp(Long.parseLong(timestampString))
                        .withSummary(summary)
                        .build();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
