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

import com.android.ddmlib.logcat.*;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.gson.Gson;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;

import java.util.*;

class LogCatTestRunListener implements ITestRunListener {
    private final FileManager fileManager;
    private final Pool pool;
	private final Device device;
	private final LogCatReceiverTask logCatReceiverTask;
	private final LogCatListener logCatListener;
	private final List<LogCatMessage> logCatMessages;
    private final Gson gson;

    public LogCatTestRunListener(Gson gson, FileManager fileManager, Pool pool, Device device) {
        this.gson = gson;
        this.fileManager = fileManager;
        this.pool = pool;
		this.device = device;
		logCatMessages = new ArrayList<>();
		logCatListener = new MessageCollectingLogCatListener(logCatMessages);
		logCatReceiverTask = new LogCatReceiverTask(device.getDeviceInterface());
	}

	@Override
	public void testRunStarted(String runName, int testCount) {
		logCatReceiverTask.addLogCatListener(logCatListener);
		new Thread(logCatReceiverTask, "CatLogger-" + runName + "-" + device.getSerial()).start();
	}

	@Override
	public void testStarted(TestIdentifier test) {
	}

	@Override
	public void testFailed(TestIdentifier test, String trace) {
	}

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
    }

    @Override
    public void testIgnored(TestIdentifier test) {
    }

    @Override
	public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
		List<LogCatMessage> copyOfLogCatMessages;
		synchronized (logCatMessages) {
			int size = logCatMessages.size();
			copyOfLogCatMessages = new ArrayList<>(size);
			copyOfLogCatMessages.addAll(logCatMessages);
		}
        LogCatWriter logCatWriter = new CompositeLogCatWriter(
                new JsonLogCatWriter(gson, fileManager, pool, device),
                new RawLogCatWriter(fileManager, pool, device));
        LogCatSerializer logCatSerializer = new LogCatSerializer(test, logCatWriter);
		logCatSerializer.serializeLogs(copyOfLogCatMessages);
	}

	@Override
	public void testRunFailed(String errorMessage) {
	}

	@Override
	public void testRunStopped(long elapsedTime) {
	}

	@Override
	public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
		logCatReceiverTask.stop();
		logCatReceiverTask.removeLogCatListener(logCatListener);
	}

	private final class MessageCollectingLogCatListener implements LogCatListener {
		private final List<LogCatMessage> logCatMessages;

		public MessageCollectingLogCatListener(List<LogCatMessage> messageList) {
			logCatMessages = messageList;
		}

		@Override
		public void log(List<LogCatMessage> msgList) {
			synchronized (logCatMessages) {
				logCatMessages.addAll(msgList);
			}
		}
	}
}
