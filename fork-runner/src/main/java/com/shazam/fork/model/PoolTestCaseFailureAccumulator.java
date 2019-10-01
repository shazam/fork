/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.model;

import com.google.common.collect.SetMultimap;

import java.util.function.Predicate;

import static com.google.common.collect.HashMultimap.create;

/**
 * Class that keeps track of the number of times each testCase is executed for device.
 */
public class PoolTestCaseFailureAccumulator implements PoolTestCaseAccumulator {
    private final SetMultimap<Pool, TestCaseEventCounter> testCaseCounters = create();

    @Override
    public void record(Pool pool, TestCaseEvent testCaseEvent) {
        synchronized (testCaseCounters) {
            if (!testCaseCounters.containsKey(pool)) {
                testCaseCounters.put(pool, createNew(testCaseEvent));
            }

            boolean hasCountedBefore = testCaseCounters.get(pool).stream()
                    .anyMatch(isSameTestCase(testCaseEvent));
            if (hasCountedBefore) {
                testCaseCounters.get(pool).stream()
                        .filter(isSameTestCase(testCaseEvent))
                        .findFirst()
                        .ifPresent(TestCaseEventCounter::increaseCount);
            } else {
                testCaseCounters.get(pool).add(createNew(testCaseEvent).withIncreasedCount());
            }
        }
    }

    @Override
    public int getCount(Pool pool, TestCaseEvent testCaseEvent) {
        if (testCaseCounters.containsKey(pool)) {
            synchronized (testCaseCounters) {
                return testCaseCounters.get(pool).stream()
                        .filter(isSameTestCase(testCaseEvent))
                        .findFirst()
                        .orElse(TestCaseEventCounter.EMPTY)
                        .getCount();
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getCount(TestCaseEvent testCaseEvent) {
        synchronized (testCaseCounters) {
            return testCaseCounters.values().stream()
                    .filter(isSameTestCase(testCaseEvent))
                    .mapToInt(TestCaseEventCounter::getCount)
                    .sum();
        }
    }

    private static TestCaseEventCounter createNew(final TestCaseEvent testCaseEvent) {
        return new TestCaseEventCounter(testCaseEvent, 0);
    }

    private static Predicate<TestCaseEventCounter> isSameTestCase(TestCaseEvent testCaseEvent) {
        return testCaseEventCounter -> testCaseEventCounter.getTestCaseEvent().equals(testCaseEvent);
    }
}
