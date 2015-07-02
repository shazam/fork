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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.shazam.fork.reporter.model.*;
import com.shazam.fork.summary.*;

import java.util.*;

import static com.shazam.fork.reporter.model.Build.Builder.aBuild;
import static com.shazam.fork.reporter.model.FlakinessReport.Builder.flakinessReport;
import static com.shazam.fork.reporter.model.PoolHistory.Builder.poolHistory;
import static com.shazam.fork.reporter.model.TestInstance.Builder.testInstance;
import static com.shazam.fork.reporter.model.TestLabel.Builder.testLabel;
import static com.shazam.fork.utils.ReadableNames.readablePoolName;
import static java.util.stream.Collectors.toList;

public class FlakinessCalculator {

    public FlakinessReport calculate(Executions executions) throws FlakinessCalculationException {
        List<Execution> executionsList = executions.getExecutions();
        List<Build> builds = new ArrayList<>(executionsList.size());
        Set<TestLabel> testLabels = new HashSet<>();
        Set<String> poolNames = new HashSet<>();

        HashMap<String, Table<TestLabel, Build, TestInstance>> rawResultsTable = reduceResults(executionsList, builds, testLabels, poolNames);
        //TODO Score and sort table rows.
        List<PoolHistory> poolHistories = getPoolHistories(rawResultsTable);

        return flakinessReport()
                .withName("Shazam on Android")
                .withPoolHistories(poolHistories)
                .build();
    }

    private HashMap<String, Table<TestLabel, Build, TestInstance>> reduceResults(
            List<Execution> executionsList,
            List<Build> builds,
            Set<TestLabel> testLabels,
            Set<String> poolNames) {
        HashMap<String, Table<TestLabel, Build, TestInstance>> poolToFlakinessTableMap = new HashMap<>();
        for (Execution execution : executionsList) {
            String buildId = execution.getBuildId();
            Build build = aBuild().withBuildId(buildId).build();
            builds.add(build);

            List<PoolSummary> poolSummaries = execution.getSummary().getPoolSummaries();
            for (PoolSummary poolSummary : poolSummaries) {
                String poolName = poolSummary.getPoolName();
                poolNames.add(poolName);
                Table<TestLabel, Build, TestInstance> table = getOrCreateTable(poolToFlakinessTableMap, poolName);
                for (TestResult testResult : poolSummary.getTestResults()) {
                    TestLabel testLabel = testLabel()
                            .withClassName(testResult.getTestClass())
                            .withMethod(testResult.getTestMethod())
                            .build();
                    testLabels.add(testLabel);

                    ResultStatus resultStatus = testResult.getResultStatus();
                    TestInstance testInstance = testInstance().withResultStatus(resultStatus).build();
                    table.put(testLabel, build, testInstance);
                }
            }
        }
        return poolToFlakinessTableMap;
    }

    private Table<TestLabel, Build, TestInstance> getOrCreateTable(HashMap<String, Table<TestLabel, Build, TestInstance>> poolToFlakinessTableMap, String poolName) {
        Table<TestLabel, Build, TestInstance> table = poolToFlakinessTableMap.get(poolName);
        if (table == null) {
            table = HashBasedTable.create();
            poolToFlakinessTableMap.put(poolName, table);
        }
        return table;
    }

    private List<PoolHistory> getPoolHistories(HashMap<String, Table<TestLabel, Build, TestInstance>> rawResultsTable) {
        return rawResultsTable.entrySet().stream()
                .map(poolToTable -> poolHistory()
                        .withName(poolToTable.getKey())
                        .withReadableName(readablePoolName(poolToTable.getKey()))
                        .withHistoryTable(poolToTable.getValue())
                        .build())
                .collect(toList());
    }
}
