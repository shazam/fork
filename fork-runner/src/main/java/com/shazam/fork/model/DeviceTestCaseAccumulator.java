package com.shazam.fork.model;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.FluentIterable.from;

/**
 * Class that keeps track of the number of times each testCase is executed for device.
 */
public class DeviceTestCaseAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestCaseAccumulator.class);

    private Multimap<Device, TestCaseEventCounter> map = ArrayListMultimap.create();

    public void record(Device device, TestCaseEvent testCaseEvent) {

        if (!map.containsKey(device)) {
            map.put(device, createNew(testCaseEvent).get());
        }

        if (!from(map.get(device)).anyMatch(isSameTestCase(testCaseEvent))) {
            TestCaseEventCounter aNew = createNew(testCaseEvent).get();
            aNew.increaseCount();
            map.get(device).add(aNew);
        } else {
            from(map.get(device)).firstMatch(isSameTestCase(testCaseEvent)).get().increaseCount();
        }
    }

    public int getCount(Device device, TestCaseEvent testCaseEvent) {
        if (map.containsKey(device)) {
            return from(map.get(device))
                    .firstMatch(isSameTestCase(testCaseEvent)).or(TestCaseEventCounter.EMPTY)
                    .getCount();
        } else {
            return 0;
        }
    }

    private static Supplier<TestCaseEventCounter> createNew(final TestCaseEvent testCaseEvent) {
        return new Supplier<TestCaseEventCounter>() {
            @Override
            public TestCaseEventCounter get() {
                return new TestCaseEventCounter(testCaseEvent, 0);
            }
        };
    }

    private static Predicate<TestCaseEventCounter> isSameTestCase(final TestCaseEvent testCaseEvent) {
        return new Predicate<TestCaseEventCounter>() {
            @Override
            public boolean apply(TestCaseEventCounter input) {
                return input.getType() != null
                        && testCaseEvent.equals(input.getType());
            }
        };
    }
}
