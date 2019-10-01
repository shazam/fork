/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter;

import com.google.common.collect.*;
import com.shazam.fork.reporter.model.*;
import com.shazam.fork.summary.*;

import java.util.*;
import java.util.function.Function;

import javax.annotation.Nonnull;

import static com.google.common.collect.TreeBasedTable.create;
import static com.shazam.fork.reporter.model.Build.Builder.aBuild;
import static com.shazam.fork.reporter.model.FlakinessReport.Builder.flakinessReport;
import static com.shazam.fork.reporter.model.PoolHistory.Builder.poolHistory;
import static com.shazam.fork.reporter.model.ScoredTestLabel.Builder.scoredTestLabel;
import static com.shazam.fork.reporter.model.TestInstance.Builder.testInstance;
import static com.shazam.fork.reporter.model.TestLabel.Builder.testLabel;
import static java.util.stream.Collectors.toList;

public class FlakinessSorter {

    private final String title;
    private final BuildLinkCreator buildLinkCreator;
    private final TestLinkCreator testLinkCreator;

    public FlakinessSorter(String title, BuildLinkCreator buildLinkCreator, TestLinkCreator testLinkCreator) {
        this.title = title;
        this.buildLinkCreator = buildLinkCreator;
        this.testLinkCreator = testLinkCreator;
    }

    public FlakinessReport sort(Executions executions) throws FlakinessCalculationException {
        List<Execution> executionsList = executions.getExecutions();
        List<Build> buildsFullIndex = new ArrayList<>(executionsList.size());
        Set<TestLabel> testLabelsFullIndex = new HashSet<>();
        Set<String> poolNamesFullIndex = new HashSet<>();

        List<UnsortedPoolHistory> unsortedPoolHistories = reduceToUnsortedHistories(executionsList, buildsFullIndex,
                testLabelsFullIndex, poolNamesFullIndex);
        List<PoolHistory> poolHistories = sortHistories(unsortedPoolHistories, buildsFullIndex, testLabelsFullIndex);

        return flakinessReport()
                .withTitle(title)
                .withPoolHistories(poolHistories)
                .build();
    }

    private List<PoolHistory> sortHistories(List<UnsortedPoolHistory> unsortedPoolHistories, List<Build> buildsFullIndex, Set<TestLabel> testLabelsFullIndex) {
        return unsortedPoolHistories.stream()
                .map(sortPoolHistory(buildsFullIndex, testLabelsFullIndex))
                .collect(toList());
    }

    private List<UnsortedPoolHistory> reduceToUnsortedHistories(
            List<Execution> executionsList,
            List<Build> builds,
            Set<TestLabel> testLabels,
            Set<String> poolNames) {
        HashMap<String, Table<TestLabel, Build, TestInstance>> poolToFlakinessTableMap = new HashMap<>();
        for (Execution execution : executionsList) {
            String buildId = execution.getBuildId();
            String buildLink = buildLinkCreator.createLink(buildId);
            Build build = aBuild()
                    .withBuildId(buildId)
                    .withLink(buildLink)
                    .build();
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

                    TestInstance testInstance = testInstance()
                            .withResultStatusFrom(testResult)
                            .withLink(testLinkCreator.createLinkToTest(buildLink, poolSummary.getPoolName(), testLabel))
                            .build();
                    table.put(testLabel, build, testInstance);
                }
            }
        }

        return poolToFlakinessTableMap.entrySet().stream()
                .map(entry -> new UnsortedPoolHistory(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    private Table<TestLabel, Build, TestInstance> getOrCreateTable(HashMap<String, Table<TestLabel, Build, TestInstance>> poolToFlakinessTableMap, String poolName) {
        Table<TestLabel, Build, TestInstance> table = poolToFlakinessTableMap.get(poolName);
        if (table == null) {
            table = HashBasedTable.create();
            poolToFlakinessTableMap.put(poolName, table);
        }
        return table;
    }

    @Nonnull
    private Function<UnsortedPoolHistory, PoolHistory> sortPoolHistory(List<Build> buildsFullIndex, Set<TestLabel> testLabelsFullIndex) {
        return tableEntry -> {
            TreeBasedTable<ScoredTestLabel, Build, TestInstance> sortedTable = sortTable(tableEntry.getHistoryTable(),
                    buildsFullIndex, testLabelsFullIndex);
            return poolHistory()
                    .withName(tableEntry.getName())
                    .withHistoryTable(sortedTable)
                    .build();
        };
    }

    private TreeBasedTable<ScoredTestLabel, Build, TestInstance> sortTable(Table<TestLabel, Build, TestInstance> rawResultsTable,
                                                                           List<Build> buildsFullIndex,
                                                                           Set<TestLabel> testLabelsFullIndex) {

        TreeBasedTable<ScoredTestLabel, Build, TestInstance> sortedTable = create(
                (scoredTest1, scoredTest2) -> scoredTest1.getTestScore().compareTo(scoredTest2.getTestScore()),
                Build::compareTo);

        for (TestLabel testLabel : testLabelsFullIndex) {
            List<TestInstance> testInstances = collectInstancesOfTest(rawResultsTable, buildsFullIndex, testLabel);

            TestScore testScore = TestScore.from(testLabel, testInstances);
            ScoredTestLabel scoredTestLabel = scoredTestLabel()
                    .withTestLabel(testLabel)
                    .withTestScore(testScore)
                    .build();
            for (int i = 0; i < buildsFullIndex.size(); i++) {
                sortedTable.put(scoredTestLabel, buildsFullIndex.get(i), testInstances.get(i));
            }
        }
        return sortedTable;
    }

    @Nonnull
    private List<TestInstance> collectInstancesOfTest(Table<TestLabel, Build, TestInstance> rawResultsTable,
                                                      List<Build> buildsFullIndex,
                                                      TestLabel testLabel) {
        return buildsFullIndex.stream()
                .map(build -> {
                    TestInstance testInstance = rawResultsTable.get(testLabel, build);
                    return (testInstance == null ? testInstance().build() : testInstance);
                })
                .collect(toList());
    }
}
