/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter.model;

import java.util.List;

import javax.annotation.Nonnull;

public class TestScore implements Comparable<TestScore> {
    private static final char SCORE_FAIL = 'f';
    private static final char SCORE_MISSING = 'm';
    private static final char SCORE_PASS = 'p';

    private final String score;

    private TestScore(String score) {
        this.score = score;
    }

    public static TestScore from(TestLabel testLabel, List<TestInstance> testInstances) {
        StringBuilder score = new StringBuilder(testInstances.size());
        testInstances.stream().forEach(testInstance -> {
            Status status = testInstance.getStatus();
            char scoreCode = scoreCodeFor(status);
            score.append(scoreCode);
        });

        return new TestScore(score
                .reverse()
                .append(testLabel.getClassName())
                .append(testLabel.getMethod())
                .toString());
    }

    private static char scoreCodeFor(@Nonnull Status resultStatus) {
        switch (resultStatus) {
            case PASS:
                return SCORE_PASS;
            case FAIL:
                return SCORE_FAIL;
            case MISSING:
                return SCORE_MISSING;
            default:
                return SCORE_MISSING;
        }
    }

    @Override
    public int compareTo(@Nonnull TestScore otherTestScore) {
        String otherScore = otherTestScore.score;

        return score.compareTo(otherScore);
    }
}
