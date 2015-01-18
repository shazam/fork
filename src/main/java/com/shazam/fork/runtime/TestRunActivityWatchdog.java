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

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
* Aborts lingering tests
*/
public class TestRunActivityWatchdog implements ITestRunListener {

	private static final Map<String,String> EMPTY_MAP = Collections.emptyMap();
    private static final Logger logger = LoggerFactory.getLogger(TestRunActivityWatchdog.class);

    private final Configuration configuration;
    private final RemoteAndroidTestRunner runner;
    private final TestClass test;
    private final String poolName;
    private final ITestRunListener[] testRunListeners;
    private final String device;
    private final HashMap<String, Boolean> outstandingTestStarted;
    private final Timer timer;

    private TimerTask task;
	private boolean testRunStarted;
	private boolean testRunEnded;
	private boolean testRunFailed;
    private boolean barked = false;

    public TestRunActivityWatchdog(Configuration configuration, RemoteAndroidTestRunner runner, TestClass test,
                                   String poolName, ITestRunListener[] testRunListeners, String device) {
        this.configuration = configuration;
        this.runner = runner;
		this.test = test;
		this.poolName = poolName;
		this.testRunListeners = testRunListeners;
		this.device = device;
		this.outstandingTestStarted = new HashMap<>();
		for (TestMethod method : test.getUnsuppressedMethods()) {
			outstandingTestStarted.put(method.getName(), false);
		}
		timer = new Timer("Watchdog for " + test, true);
		interTestReschedule("testRunWatchdogCreated");
	}

	private void reschedule(int delay, TimerTask newTask) {
		if (task != null) {
			task.cancel();
			timer.purge();
		}
		task = newTask;
		timer.schedule(task, delay);
	}

	private void interTestReschedule(final String reason) {
		reschedule(configuration.getTestIntervalTimeout(), new TimerTask() {
			@Override
			public void run() {
				barked = true;
                logger.warn("Cancelling {} on {} due to timeout after {}", test.getClassName(), device, reason);
				runner.cancel();
			}
		});
	}

	private void rescheduleWithAbort(final String method) {
		reschedule(configuration.getTestTimeout(), new TimerTask() {
			@Override
			public void run() {
				barked = true;
				logger.warn("Failing {}.{} on {} due to timeout after test started", test.getClassName(), method, device);
				runner.cancel();
				// Fail cancelled test to try to capture logcat.
				flagTestError(method, outstandingTestStarted.get(method), "Timed out: aborting " + method, testRunListeners);
				observeTestCompleted(new TestIdentifier(test.getClassName(), method));
			}
		});
	}

	public void cancel() {
		timer.cancel();
	}

	private void observeTestCompleted(TestIdentifier testIdentifier) {
		outstandingTestStarted.remove(testIdentifier.getTestName());
	}

	private void observeTestStarted(TestIdentifier testIdentifier) {
		outstandingTestStarted.put(testIdentifier.getTestName(), true);
	}

	// I'm the first listener, so... I should be able to lie to the other listeners.
    //TODO Refactor
	public void flagOutstandingAsErrors(String lastWatchdogFailure) {
		Set<String> methods = new HashSet<>(outstandingTestStarted.keySet());
		int outstandingTests = methods.size();
		if (!barked || outstandingTests == 0) {
			// No interference necessary if everything ran properly and we didn't bark.
			return;
		}

		if (!testRunStarted) {
			for (ITestRunListener testRunListener : testRunListeners) {
				testRunListener.testRunStarted(poolName, outstandingTests);
			}
		}

		for (String testMethod : methods) {
			for (ITestRunListener testRunListener : testRunListeners) {
				Boolean previouslyStarted = outstandingTestStarted.get(testMethod);
				if (previouslyStarted != null) {
					flagTestError(testMethod, previouslyStarted, lastWatchdogFailure + " Tests not run: " + outstandingTests + " : " + methods, testRunListener);
				}
			}
		}
		outstandingTestStarted.clear();

		if (!testRunFailed) {
			testRunFailed = true;
			for (ITestRunListener testRunListener : testRunListeners) {
				testRunListener.testRunFailed(lastWatchdogFailure);
			}
		}

		if (!testRunEnded) {
			testRunEnded = true;
			for (ITestRunListener testRunListener : testRunListeners) {
				testRunListener.testRunEnded(0, EMPTY_MAP);
			}
		}
	}

	private void flagTestError(String testMethod, boolean previouslyStarted, String lastWatchdogFailure, ITestRunListener... testRunListeners) {
		TestIdentifier identifier = new TestIdentifier(test.getClassName(), testMethod);
		for (ITestRunListener testRunListener : testRunListeners) {
			if (!previouslyStarted) {
				testRunListener.testStarted(identifier);
			}
			testRunListener.testFailed(identifier, "WATCHDOG: " + lastWatchdogFailure);
			testRunListener.testEnded(identifier, EMPTY_MAP);
		}
	}

	@Override
	/** Mandatory on test run start */
	public void testRunStarted(String runName, int testCount) {
		interTestReschedule("testRunStarted");
		this.testRunStarted = true;
	}

	@Override
	/** Mandatory on test start */
	public void testStarted(TestIdentifier testIdentifier) {
		rescheduleWithAbort(testIdentifier.getTestName());
		observeTestStarted(testIdentifier);
	}

	@Override
	/** Called just before testEnded() on failure */
	public void testFailed(TestIdentifier testIdentifier, String errorMessage) {
		interTestReschedule("testFailed" + testIdentifier.getTestName());
		observeTestCompleted(testIdentifier);
	}

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
        logger.debug("test=%s", test);
        logger.debug("assumption failure %s", trace);
    }

    @Override
    public void testIgnored(TestIdentifier test) {
        logger.debug("ignored test %s", test);
    }

    @Override
	/** Mandatory */
	public void testEnded(TestIdentifier testIdentifier, Map<String, String> runMetrics) {
		interTestReschedule("testEnded" + testIdentifier.getTestName());
		observeTestCompleted(testIdentifier);
	}

	@Override
	/** Called just before testRunEnded() on failure */
	public void testRunFailed(String errorMessage) {
		String reason = "testRunFailed: " + errorMessage;
		timer.cancel();
		flagOutstandingAsErrors(reason);
		this.testRunFailed = true;
	}

	@Override
	/** Allegedly unused, nominally reports a cancel(). */
	public void testRunStopped(long elapsedTime) {
		String reason = String.format("testRunStopped (%d)", elapsedTime);
		timer.cancel();
		flagOutstandingAsErrors(reason);
	}

	@Override
	/** Mandatory, final callback ever called. */
	public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
		timer.cancel();
		flagOutstandingAsErrors("testRunEnded");
		this.testRunEnded = true;
	}
}
