/*
 * Copyright 2018 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.model;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.HashMultimap.create;

/**
 * Class that keeps track of the number of times each testCase is executed for device.
 */
public class PoolTestCaseFailureAccumulator implements PoolTestCaseAccumulator {
    private SetMultimap<Pool, TestCaseEventCounter> map = Multimaps.synchronizedSetMultimap(create());

    @Override
    public void record(Pool pool, TestCaseEvent testCaseEvent) {
        if (!map.containsKey(pool)) {
            map.put(pool, createNew(testCaseEvent));
        }

        if (!from(map.get(pool)).anyMatch(isSameTestCase(testCaseEvent))) {
            map.get(pool).add(
                    createNew(testCaseEvent)
                            .withIncreasedCount());
        } else {
            from(map.get(pool))
                    .firstMatch(isSameTestCase(testCaseEvent)).get()
                    .increaseCount();
        }
    }

    @Override
    public int getCount(Pool pool, TestCaseEvent testCaseEvent) {
        if (map.containsKey(pool)) {
            return from(map.get(pool))
                    .firstMatch(isSameTestCase(testCaseEvent)).or(TestCaseEventCounter.EMPTY)
                    .getCount();
        } else {
            return 0;
        }
    }

    @Override
    public int getCount(TestCaseEvent testCaseEvent) {
        int result = 0;
        ImmutableList<TestCaseEventCounter> counters = from(map.values())
                .filter(isSameTestCase(testCaseEvent)).toList();
        for (TestCaseEventCounter counter : counters) {
            result += counter.getCount();
        }
        return result;
    }

    private static TestCaseEventCounter createNew(final TestCaseEvent testCaseEvent) {
        return new TestCaseEventCounter(testCaseEvent, 0);
    }

    private static Predicate<TestCaseEventCounter> isSameTestCase(final TestCaseEvent testCaseEvent) {
        return new Predicate<TestCaseEventCounter>() {
            @Override
            public boolean apply(TestCaseEventCounter input) {
                return input.getTestCaseEvent() != null
                        && testCaseEvent.equals(input.getTestCaseEvent());
            }
        };
    }
}
