/*
 * Copyright 2019 Apple Inc.
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
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;

import java.io.*;
import java.util.List;

import static com.shazam.fork.system.io.FileType.RAW_LOG;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.write;

class RawLogCatWriter implements LogCatWriter {
    private final FileManager fileManager;
	private final Pool pool;
	private final Device device;

	RawLogCatWriter(FileManager fileManager, Pool pool, Device device) {
        this.fileManager = fileManager;
		this.pool = pool;
		this.device = device;
	}

	@Override
	public void writeLogs(TestIdentifier test, List<LogCatMessage> logCatMessages) {
        File file = fileManager.createFile(RAW_LOG, pool, device, test);
        FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			for (LogCatMessage logCatMessage : logCatMessages) {
				write(logCatMessage.toString(), fileWriter);
				write("\n", fileWriter);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(fileWriter);
		}
	}
}
