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
import com.shazam.fork.reporter.html.*;
import com.shazam.fork.reporter.model.*;
import com.shazam.fork.summary.ResultStatus;

import java.util.List;
import java.util.Set;

import static com.shazam.fork.reporter.html.HtmlTestInstance.Status.*;
import static java.util.stream.Collectors.toList;

public class TestToHtmlFlakinessReportConverter {

    public HtmlFlakyTestIndex convertToIndex(FlakinessReport flakinessReport) {
        List<PoolOption> options = flakinessReport.getPoolHistories().stream()
                .map(poolHistory -> new PoolOption(poolHistory.getName(), poolHistory.getReadableName()))
                .collect(toList());
        return new HtmlFlakyTestIndex(flakinessReport.getName(), options);
    }

    public List<HtmlFlakyTestPool> convertToPools(FlakinessReport flakinessReport) {
        return flakinessReport.getPoolHistories().stream()
                .map(this::convertToPoolHtml)
                .collect(toList());

    }

    private HtmlFlakyTestPool convertToPoolHtml(PoolHistory poolHistory) {
        Table<TestLabel, Build, TestInstance> table = poolHistory.getHistoryTable();
        Set<Build> builds = table.columnKeySet();
        Set<TestLabel> testLabels = table.rowKeySet();

        return new HtmlFlakyTestPool(
                poolHistory.getName(),
                stringBuildIds(builds),
                createHtmlTestHistories(table, builds, testLabels));
    }

    private List<String> stringBuildIds(Set<Build> builds) {
        return builds.stream()
                .map(build -> build.getBuildId())
                .collect(toList());
    }

    private List<HtmlTestHistory> createHtmlTestHistories(Table<TestLabel, Build, TestInstance> table,
                                                          Set<Build> builds,
                                                          Set<TestLabel> testLabels) {
        return testLabels.stream()
                .map(testLabel -> {
                    List<HtmlTestInstance> htmlTestInstances = builds.stream()
                            .map(build -> htmlTestInstanceFrom(table.get(testLabel, build)))
                            .collect(toList());
                    return new HtmlTestHistory(testLabel.getClassName(), testLabel.getMethod(), htmlTestInstances);
                })
                .collect(toList());

    }

    private HtmlTestInstance htmlTestInstanceFrom(TestInstance testInstance) {
        HtmlTestInstance.Status status = PASS;
        if (testInstance == null) {
            status = MISSING;
        } else if (testInstance.getResultStatus() == ResultStatus.ERROR || testInstance.getResultStatus() == ResultStatus.FAIL) {
            status = FAIL;
        }

        return new HtmlTestInstance(status);
    }
}
