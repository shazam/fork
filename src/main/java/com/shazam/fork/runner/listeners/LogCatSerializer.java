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
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.testrunner.TestIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;

class LogCatSerializer {
	private final Pattern testStartPattern;
	private final Pattern testEndPattern;
	private final LogCatWriter logCatWriter;
	private final TestIdentifier test;

	LogCatSerializer(TestIdentifier test, LogCatWriter logCatWriter) {
		this.test = test;
		this.logCatWriter = logCatWriter;
		testStartPattern = Pattern.compile(createPattern("started", test));
		testEndPattern = Pattern.compile(createPattern("finished", test));
	}

	public void serializeLogs(List<LogCatMessage> logCatMessages) {
		List<LogCatMessage> filterLogCatMessages = filterLogCatMessages(logCatMessages);
		logCatWriter.writeLogs(test, filterLogCatMessages);
	}

    //TODO No need to start and stop writing, we could just get index for start & stop and get inline sublist (no new list)
	private List<LogCatMessage> filterLogCatMessages(List<LogCatMessage> logCatMessages) {
		boolean isWriting = false;
		List<LogCatMessage> filteredLogCatMessages = new ArrayList<>();
		for (LogCatMessage logCatMessage : logCatMessages) {
			if (testStartPattern.matcher(logCatMessage.getMessage()).find()) {
				isWriting = true;
			}
			if (isWriting) {
				filteredLogCatMessages.add(logCatMessage);
			}
			if (testEndPattern.matcher(logCatMessage.getMessage()).find()) {
				isWriting = false;
			}
		}
		return filteredLogCatMessages;
	}

	private String createPattern(String action, TestIdentifier test) {
        return format("%s:\\s+\\Q%s\\E\\(\\Q%s\\E\\)", action, test.getTestName(), test.getClassName());
    }
}
