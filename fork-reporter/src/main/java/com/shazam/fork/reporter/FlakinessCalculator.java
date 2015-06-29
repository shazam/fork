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

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import com.shazam.fork.reporter.model.*;
import com.shazam.fork.summary.ResultStatus;

import java.util.List;

import static com.shazam.fork.reporter.model.Build.Builder.aBuild;
import static com.shazam.fork.reporter.model.FlakinessReport.Builder.flakinessReport;
import static com.shazam.fork.reporter.model.PoolHistory.Builder.poolHistory;
import static com.shazam.fork.reporter.model.TestInstance.Builder.testInstance;
import static com.shazam.fork.reporter.model.TestLabel.Builder.testLabel;
import static java.util.Arrays.asList;

public class FlakinessCalculator {
    public FlakinessReport calculate(Executions executions) {
        return HARDCODED_flakinessReport();
    }

    private FlakinessReport HARDCODED_flakinessReport() {
        List<PoolHistory> poolHistories = asList(
                HARDCODED_poolHistory1(),
                HARDCODED_poolHistory1()
        );
        return flakinessReport()
                .withName("Shazam on Android Master")
                .withPoolHistories(poolHistories)
                .build();
    }

    private PoolHistory HARDCODED_poolHistory1() {
        List<TestLabel> tests = tests();
        List<Build> builds = builds();

        Table<TestLabel, Build, TestInstance> table = ArrayTable.<TestLabel, Build, TestInstance>create(tests, builds);
        for (TestLabel testLabel : tests) {
            for (Build build : builds) {
                TestInstance testInstance = testInstance()
                        .withResultStatus(ResultStatus.PASS)
                        .withLink("#")
                        .build();
                table.put(testLabel, build, testInstance);
            }
        }

        return poolHistory()
                .withName("all=sw0-up")
                .withReadableName("Phones Sw0 up")
                .withHistoryTable(table)
                .build();
    }

    private List<TestLabel> tests() {
        return asList(
                label("com.shazam.android.acceptancetests.musicdetails.streaming.MusicDetailsTaggingWithRdioTest", "userDoesNotSeeAdAfterSuccessfulTag"),
                label("com.shazam.android.acceptancetests.SanityTest", "hasFacebookInstalled"),
                label("com.shazam.android.acceptancetests.musicdetails.streaming.RdioTest", "userCanListenToMusic"),
                label("com.shazam.android.acceptancetests.notifications.gcm.Notificationstest", "goesToExploreScreen")
        );
    }

    private TestLabel label(String testClass, String method) {
        return testLabel()
                .withClassName(testClass)
                .withMethod(method)
                .build();
    }

    private List<Build> builds() {
        return asList(build("1751"), build("1752"), build("1753"), build("1754"), build("1755"));
    }

    private Build build(String buildId) {
        return aBuild()
                .withBuildId(buildId)
                .build();
    }

}
