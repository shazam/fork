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

import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.model.Diagnostics.SCREENSHOTS;
import static com.shazam.fork.model.Diagnostics.VIDEO;
import static com.shazam.fork.summary.OutcomeAggregator.toPoolOutcome;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

class HtmlConverters {

	public static HtmlSummary toHtmlSummary(Summary summary) {
		HtmlSummary htmlSummary = new HtmlSummary();
		htmlSummary.title = summary.getTitle();
		htmlSummary.subtitle = summary.getSubtitle();
		htmlSummary.pools = transform(summary.getPoolSummaries(), toHtmlPoolSummary());
		htmlSummary.ignoredTests = summary.getIgnoredTests();
        htmlSummary.overallStatus = new OutcomeAggregator().aggregate(summary) ? "pass" : "fail";
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
				htmlPoolSummary.prettyPoolName = prettifyPoolName(poolName);
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
				htmlTestResult.status = input.getResultStatus().name().toLowerCase();
				htmlTestResult.prettyClassName = prettifyClassName(input.getTestClass());
				htmlTestResult.prettyMethodName = prettifyMethodName(input.getTestMethod());
				htmlTestResult.timeTaken = String.format("%.2f", input.getTimeTaken());
				htmlTestResult.plainMethodName = input.getTestMethod();
				htmlTestResult.plainClassName = input.getTestClass();
				htmlTestResult.poolName = poolName;
				htmlTestResult.trace = input.getTrace().split("\n");
				// Keeping logcats in memory is hugely wasteful. Now they're read at page-creation.
				// htmlTestResult.logcatMessages = transform(input.getLogCatMessages(), toHtmlLogCatMessages());
				Device device = input.getDevice();
				htmlTestResult.deviceSerial = device.getSerial();
				htmlTestResult.deviceModelDespaced = device.getModelName().replace(" ", "_");
                Diagnostics supportedDiagnostics = device.getSupportedDiagnostics();
                htmlTestResult.diagnosticVideo = VIDEO.equals(supportedDiagnostics);
                htmlTestResult.diagnosticScreenshots = SCREENSHOTS.equals(supportedDiagnostics);
				return htmlTestResult;
			}
		};
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
				htmlLogCatMessage.time = logCatMessage.getTime();
				return htmlLogCatMessage;
			}
		};
	}

	private static String prettifyPoolName(String poolName) {
		return capitalizeFully(poolName.replaceAll("[\\W]|_", " "));
	}

	private static String prettifyClassName(String testClass) {
		final int lastIndexOfDot = testClass.lastIndexOf('.');
		if (lastIndexOfDot != -1) {
			testClass = testClass.substring(lastIndexOfDot+1);
		}
		return testClass;
	}

	private static String prettifyMethodName(String testMethod) {
		testMethod = testMethod
			.replaceFirst("test", "")
			.replaceAll("_", ", ")
			.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2")
			.replaceAll("(\\p{Lu})(\\p{Lu})","$1 $2");
		return capitalizeFully(testMethod);
	}
}
