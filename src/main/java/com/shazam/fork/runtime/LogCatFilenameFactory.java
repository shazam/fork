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

import static java.lang.String.format;

public class LogCatFilenameFactory {
	private static final String EXT = "log";
	public static final String JSON = "json";
	private static final String LOGCAT_PREFIX = "logcat__%s__%s__%s";
	private static final String FILE_OUTPUT_PATTERN = LOGCAT_PREFIX + ".%s";

	public static String createRawLogcatFilename(String poolName, String serial, TestIdentifier testIdentifier) {
		return createFilename(poolName, serial, testIdentifier.toString(), EXT);
	}

	public static String createJsonLogcatFilename(String poolName, String serial, TestIdentifier testIdentifier) {
		return createFilename(poolName, serial, testIdentifier.toString(), JSON);
	}

	public static String createLogCatFilenamePrefix(String poolName, String serial, TestIdentifier testIdentifier) {
		return format(LOGCAT_PREFIX, poolName, serial, testIdentifier.toString());
	}

	private static String createFilename(String poolName, String serial, String test, String suffix) {
		return format(FILE_OUTPUT_PATTERN, poolName, serial, test, suffix);
	}
}
