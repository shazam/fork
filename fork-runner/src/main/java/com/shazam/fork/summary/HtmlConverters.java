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
package com.shazam.fork.summary;

import com.android.ddmlib.logcat.LogCatMessage;
import com.google.common.base.Function;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Diagnostics;
import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.model.Diagnostics.SCREENSHOTS;
import static com.shazam.fork.model.Diagnostics.VIDEO;
import static com.shazam.fork.summary.ResultStatus.IGNORED;
import static com.shazam.fork.summary.ResultStatus.PASS;
import static com.shazam.fork.utils.ReadableNames.readableClassName;
import static com.shazam.fork.utils.ReadableNames.readablePoolName;
import static com.shazam.fork.utils.ReadableNames.readableTestMethodName;

class HtmlConverters {

	public static HtmlSummary toHtmlSummary(boolean isSuccessful, Summary summary) {
		HtmlSummary htmlSummary = new HtmlSummary();
		htmlSummary.title = summary.getTitle();
		htmlSummary.subtitle = summary.getSubtitle();
		htmlSummary.pools = transform(summary.getPoolSummaries(), toHtmlPoolSummary());
		htmlSummary.ignoredTests = summary.getIgnoredTests();
		htmlSummary.failedTests = summary.getFailedTests();
        htmlSummary.fatalCrashedTests = summary.getFatalCrashedTests();
        htmlSummary.overallStatus = isSuccessful ? "pass" : "fail";
		return htmlSummary;
	}

	private static Function<PoolSummary, HtmlPoolSummary> toHtmlPoolSummary() {
		return new Function<PoolSummary, HtmlPoolSummary> () {
			@Override
			@Nullable
			public HtmlPoolSummary apply(@Nullable PoolSummary poolSummary) {
				HtmlPoolSummary htmlPoolSummary = new HtmlPoolSummary();
                htmlPoolSummary.poolStatus = getPoolStatus(poolSummary);
				String poolName = poolSummary.getPoolName();
				htmlPoolSummary.prettyPoolName = readablePoolName(poolName);
                htmlPoolSummary.plainPoolName = poolName;
                htmlPoolSummary.testCount = poolSummary.getTestResults().size();
                htmlPoolSummary.testResults = transform(poolSummary.getTestResults(), toHtmlTestResult(poolName));
				return htmlPoolSummary;
			}

            private String getPoolStatus(PoolSummary poolSummary) {
                Boolean success = toPoolOutcome().apply(poolSummary);
                return (success != null && success? "pass" : "fail");
            }
        };
	}

	private static Function<TestResult, HtmlTestResult> toHtmlTestResult(final String poolName) {
		return new Function<TestResult, HtmlTestResult>(){
			@Override
			@Nullable
			public HtmlTestResult apply(@Nullable TestResult input) {
				HtmlTestResult htmlTestResult = new HtmlTestResult();
				htmlTestResult.status = computeStatus(input);
				htmlTestResult.prettyClassName = readableClassName(input.getTestClass());
				htmlTestResult.prettyMethodName = readableTestMethodName(input.getTestMethod());
				htmlTestResult.timeTaken = String.format("%.2f", input.getTimeTaken());
				htmlTestResult.plainMethodName = input.getTestMethod();
				htmlTestResult.plainClassName = input.getTestClass();
				htmlTestResult.poolName = poolName;
				htmlTestResult.trace = input.getTrace().split("\n");
				// Keeping logcats in memory is hugely wasteful. Now they're read at page-creation.
				// htmlTestResult.logcatMessages = transform(input.getLogCatMessages(), toHtmlLogCatMessages());
				Device device = input.getDevice();
				htmlTestResult.deviceSerial = device.getSerial();
				htmlTestResult.deviceSafeSerial = device.getSafeSerial();
				htmlTestResult.deviceModelDespaced = device.getModelName().replace(" ", "_");
                Diagnostics supportedDiagnostics = device.getSupportedDiagnostics();
                htmlTestResult.diagnosticVideo = VIDEO.equals(supportedDiagnostics);
                htmlTestResult.diagnosticScreenshots = SCREENSHOTS.equals(supportedDiagnostics);
				return htmlTestResult;
			}
		};
	}

    private static String computeStatus(@Nullable TestResult input) {
        String result = input.getResultStatus().name().toLowerCase();
        if (input.getResultStatus() == PASS && input.getTotalFailureCount() > 0) {
            result = "warn";
        }
        return result;
    }

	private static Function<? super PoolSummary, Boolean> toPoolOutcome() {
		return new Function<PoolSummary, Boolean>() {
			@Override
			@Nullable
			public Boolean apply(@Nullable PoolSummary input) {
				final Collection<TestResult> testResults = input.getTestResults();
				final Collection<Boolean> testOutcomes = transform(testResults, toTestOutcome());
				return !testOutcomes.isEmpty() && and(testOutcomes);
			}
		};
	}

	private static Function<TestResult, Boolean> toTestOutcome() {
		return new Function<TestResult, Boolean>() {
			@Override
			@Nullable
			public Boolean apply(@Nullable TestResult input) {
				return PASS.equals(input.getResultStatus()) || IGNORED.equals(input.getResultStatus());
			}
		};
	}

	private static Boolean and(final Collection<Boolean> booleans) {
		return BooleanUtils.and(booleans.toArray(new Boolean[booleans.size()]));
	}

	public static Function<LogCatMessage, HtmlLogCatMessage> toHtmlLogCatMessages() {
		return new Function<LogCatMessage, HtmlLogCatMessage>() {
			@Nullable
			@Override
			public HtmlLogCatMessage apply(@Nullable LogCatMessage logCatMessage) {
				HtmlLogCatMessage htmlLogCatMessage = new HtmlLogCatMessage();
				htmlLogCatMessage.appName = logCatMessage.getAppName();
				htmlLogCatMessage.logLevel = logCatMessage.getLogLevel().getStringValue();
				htmlLogCatMessage.message = logCatMessage.getMessage();
				htmlLogCatMessage.pid = logCatMessage.getPid();
				htmlLogCatMessage.tag = logCatMessage.getTag();
				htmlLogCatMessage.tid = logCatMessage.getTid();
				htmlLogCatMessage.time = logCatMessage.getTimestamp().toString();
				return htmlLogCatMessage;
			}
		};
	}
}
