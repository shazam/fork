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

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.shazam.fork.reporter.html.*;
import com.shazam.fork.reporter.model.*;

import java.util.*;

import static com.shazam.fork.utils.ReadableNames.readablePoolName;
import static com.shazam.fork.utils.ReadableNames.readableTitle;
import static java.util.stream.Collectors.toList;

public class TestToHtmlFlakinessReportConverter {

    public HtmlFlakyTestIndex convertToIndex(FlakinessReport flakinessReport) {
        List<PoolOption> options = flakinessReport.getPoolHistories().stream()
                .map(poolHistory -> {
                    String poolName = poolHistory.getName();
                    return new PoolOption(poolName, readablePoolName(poolName));
                })
                .collect(toList());
        String title = readableTitle(flakinessReport.getTitle());
        return new HtmlFlakyTestIndex(title, options);
    }

    public List<HtmlFlakyTestPool> convertToPools(FlakinessReport flakinessReport) {
        return flakinessReport.getPoolHistories().stream()
                .map(this::convertToPoolHtml)
                .collect(toList());

    }

    private HtmlFlakyTestPool convertToPoolHtml(PoolHistory poolHistory) {
        TreeBasedTable<ScoredTestLabel, Build, TestInstance> table = poolHistory.getHistoryTable();
        List<Build> buildList = table.columnKeySet().stream().collect(toList());
        SortedSet<ScoredTestLabel> testLabels = table.rowKeySet();

        return new HtmlFlakyTestPool(
                poolHistory.getName(),
                buildList,
                createHtmlTestHistories(table, buildList, testLabels));
    }

    private List<HtmlTestHistory> createHtmlTestHistories(Table<ScoredTestLabel, Build, TestInstance> table,
                                                          List<Build> builds,
                                                          SortedSet<ScoredTestLabel> testLabels) {
        return testLabels.stream()
                .map(scoredTestLabel -> {
                    List<HtmlTestInstance> htmlTestInstances = builds.stream()
                            .map(build -> {
                                TestInstance testInstance = table.get(scoredTestLabel, build);
                                return new HtmlTestInstance(testInstance.getStatus(), testInstance.getLink());
                            })
                            .collect(toList());
                    TestLabel testLabel = scoredTestLabel.getTestLabel();
                    return new HtmlTestHistory(testLabel.getClassName(), testLabel.getMethod(), htmlTestInstances);
                })
                .collect(toList());

    }
}
