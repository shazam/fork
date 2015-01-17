/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runtime;

import com.android.ddmlib.testrunner.TestIdentifier;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.shazam.fork.Utils.millisSince;
import static java.lang.System.nanoTime;

/**
 * Maintains current test state and makes it available it when requested.
 *
 * FIXME: Find a better way of keeping track of test progress.
 */

public class SwimlaneConsoleLogger {
	private static final String STARTED_NO_FAILURE = "|";
    private static final char STARTED_FIRST_FAILURE = 'a';
    private static final String STARTED_SOME_FAILURE = "abcdefghi";
    private static final String STARTED_MUCH_FAILURE = "*";
    private static final String STARTED = STARTED_NO_FAILURE + STARTED_SOME_FAILURE + STARTED_MUCH_FAILURE;
    private static final char FINISHED_FIRST_FAILURE = '1';
    private static final String FINISHED_NO_FAILURE = ".";
    private static final String FINISHED_MUCH_FAILURE = ":";
    private static final String NEVER_STARTED = "-";
    private static final String UNALLOCATED = "?";
    private static final String FAILURE = "x";

    private final Map<String, Integer> completedTestsForPool = new HashMap<>();
	private final Map<String, Integer> testClassesForPool = new HashMap<>();
	private final Map<String, String> testClass = new HashMap<>();
	private final Map<String, String> testName = new HashMap<>();
	private final Map<String, Integer> deviceFailures = new HashMap<>();
    private final Map<String, String> poolForSerial = new HashMap<>();
    private final Map<String, Integer> swimLane = new HashMap<>();
    private final SimpleDateFormat minsSecs = new SimpleDateFormat("mm.ss");
    private final List<String> status = new ArrayList<>();
    private final StatusChangedListener[] listeners;

    private long started;
    private long failures = 0;
    private int freeLane = 0;

	public interface StatusChangedListener {
		void onStatusChanged(SwimlaneConsoleLogger status);
        void complete();
	}

	public SwimlaneConsoleLogger(StatusChangedListener... listeners){
		this.listeners = listeners;
	}

	public void testStarted(String serial, TestIdentifier test) {
		setMap(serial, statusChar(serial, STARTED_NO_FAILURE, STARTED_FIRST_FAILURE, STARTED_MUCH_FAILURE));
		bumpCount(serial, test);
	}

    private String statusChar(String serial, String zero, char one, String many) {
        Integer failures = deviceFailures.get(serial);
        int fails = (failures == null) ? 0 : failures;
        if (fails == 0) {
            return zero;
        }
        if (fails > 9) {
            return many;
        }
        return String.valueOf((char)(one + fails - 1));
    }

	private void bumpCount(String serial, TestIdentifier test) {
		String lastClass = testClass.containsKey(serial) ? testClass.get(serial) : "";
		String currentClass = test.getClassName();
		testClass.put(serial, currentClass);
		testName.put(serial, test.getTestName());
		if (!currentClass.equals(lastClass)) {
			String pool = poolForSerial.get(serial);
			int tally = completedTestsForPool.containsKey(pool) ? completedTestsForPool.get(pool) : 0;
			completedTestsForPool.put(pool, tally + 1);
		}
	}

	public void setCount(String serial, String poolName, int size) {
		setMap(serial, NEVER_STARTED);
		poolForSerial.put(serial, poolName);
		if (!testClassesForPool.containsKey(poolName)) {
			testClassesForPool.put(poolName, size);
		}
		deviceFailures.put(serial, 0);
	}

	public void testFinished(String serial) {
		setMap(serial, statusChar(serial, FINISHED_NO_FAILURE, FINISHED_FIRST_FAILURE, FINISHED_MUCH_FAILURE));
	}

	public void testFailed(String serial) {
		setMap(serial, FAILURE);
		++failures;
        Integer failures = deviceFailures.get(serial);
        int fails = failures == null ? 0 : failures;
        deviceFailures.put(serial, fails + 1);
	}

	private synchronized Integer ensureSerialLaneMap(String serial) {
		if (!swimLane.containsKey(serial)) {
			status.add(freeLane, UNALLOCATED);
			swimLane.put(serial, freeLane++);
		}
		return swimLane.get(serial);
	}

	private void setMap(String serial, String i) {
		Integer index = ensureSerialLaneMap(serial);
		status.set(index, i);
		for (StatusChangedListener listener : listeners) {
			listener.onStatusChanged(this);
		}
	}

	public String getStatus(String serial) {
		ensureSerialLaneMap(serial);
		if (started == 0) {
			started = nanoTime();
		}

        StringBuilder b = new StringBuilder(minsSecs.format(new Date(millisSince(started))));
        appendTotalProgress(b);

		b.append(String.format(" %d ", failures));
        for (String testing : status) {
            b.append(testing);
        }

        String pool = poolForSerial.get(serial);
        appendPoolProgress(b, pool);

		return b.toString();
	}

    private void appendPoolProgress(StringBuilder b, String pool) {
        int percent = 0;
        if (pool != null) {
            int completed = completedTestsForPool.get(pool);
            int total = testClassesForPool.get(pool);
            percent = completed * 100 / total;
        }
        b.append(String.format("% 3d%%", percent));
    }

    private void appendTotalProgress(StringBuilder b) {
        int completed = 0;
        int total = 0;
        int percent = 0;
        for (String pool : testClassesForPool.keySet()) {
            if (completedTestsForPool.containsKey(pool) && testClassesForPool.containsKey(pool)) {
                completed += completedTestsForPool.get(pool);
                total += testClassesForPool.get(pool);
            }
        }
        if (total > 0) {
            percent = completed * 100 / total;
        }
        b.append(String.format("% 3d%%", percent));
    }

    public String pendingDevices() {
		String devices = "";
		for (String serial : swimLane.keySet()) {
			if (STARTED.contains(status.get(swimLane.get(serial)))) {
				devices += "\n[" + serial + "] " + testClass.get(serial) + "." + testName.get(serial);
			}
		}
		return devices;
	}

    public void complete() {
        for (StatusChangedListener listener : listeners) {
            listener.complete();
        }
    }
}
