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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutionReader {

    private final FileManager fileManager;

    public ExecutionReader(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public Executions readExecutions() {
        List<File> individualSummaries = fileManager.getIndividualSummaries();

        List<Summary> collect = individualSummaries.stream().map(file -> {
            //TODO Parse file into Summary
            return new Summary();
        }).collect(Collectors.toList());

        // TODO read in all of them into Summaries and then make an Executions object (concurrently).
        return null;
    }

    private class Summary {
    }
}
