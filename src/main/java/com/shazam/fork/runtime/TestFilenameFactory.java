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

import static java.lang.String.format;

public class TestFilenameFactory {
	private static final String TEST_FILE_PREFIX = "test__%s__%s";
	private static final String FILE_OUTPUT_PATTERN = TEST_FILE_PREFIX + "__%s.%s";
	private static final String XML = "xml";

	public static String createTestFilenamePrefix(String poolName, String serial) {
		return format(TEST_FILE_PREFIX, poolName, serial);
	}

	public static String createTestFilename(String poolName, String serial, String test){
		return format(FILE_OUTPUT_PATTERN, poolName, serial, test, XML);
	}

}
