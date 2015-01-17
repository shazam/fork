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

@Deprecated
public class TestFilenameFactory {
	private static final String TEST_FILE_PREFIX = "test__%s__%s";

	public static String createTestFilenamePrefix(String poolName, String serial) {
		return format(TEST_FILE_PREFIX, poolName, serial);
	}
}
