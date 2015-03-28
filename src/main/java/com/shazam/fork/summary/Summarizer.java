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

import com.google.common.base.Function;
import com.shazam.fork.*;
import com.shazam.fork.io.FileManager;
import com.shazam.fork.model.*;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.*;

import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;

public class Summarizer {

    private static final boolean STRICT = false;

    private final RuntimeConfiguration runtimeConfiguration;
    private final FileManager fileManager;
    private final Collection<DevicePool> devicePools;
    private final List<TestClass> testClasses;
    private final Serializer serializer;

    public Summarizer(RuntimeConfiguration runtimeConfiguration,
                      FileManager fileManager,
                      Collection<DevicePool> devicePools,
                      List<TestClass> testClasses) {
        this.runtimeConfiguration = runtimeConfiguration;
        this.fileManager = fileManager;
        this.devicePools = devicePools;
		this.testClasses = testClasses;
		serializer = new Persister();
	}

	public Summary compileSummary() {
		Summary.Builder summaryBuilder = aSummary();
		for (DevicePool devicePool : devicePools) {
			PoolSummary.Builder poolSummaryBuilder = aPoolSummary().withPoolName(devicePool.getName());
			compileResultsForPool(devicePool, poolSummaryBuilder);
			summaryBuilder.addPoolSummary(poolSummaryBuilder.build());
		}
        addIgnoredTests(summaryBuilder);
        summaryBuilder.withTitle(runtimeConfiguration.getTitle());
        summaryBuilder.withSubtitle(runtimeConfiguration.getSubtitle());

		return summaryBuilder.build();
	}

    private void addIgnoredTests(Summary.Builder summaryBuilder) {
        for (TestClass testClass : testClasses) {
            StringBuilder methods = new StringBuilder();
            for (TestMethod testMethod : testClass.getIgnoredMethods()) {
                methods.append(" ").append(testMethod.getName());
            }
            if (methods.length() > 0) {
                summaryBuilder.addIgnoredTest(testClass.getName() + ":" + methods);
            }
        }
    }

    private void compileResultsForPool(DevicePool devicePool, PoolSummary.Builder poolSummaryBuilder) {
		for (Device device: devicePool.getDevices()) {
			compileResultsForDevice(devicePool, poolSummaryBuilder, device);
		}
		Device watchdog = getPoolWatchdog(devicePool.getName());
		compileResultsForDevice(devicePool, poolSummaryBuilder, watchdog);
	}

	private Device getPoolWatchdog(String devicePoolName) {
		return aDevice()
					.withSerial(DevicePoolRunner.DROPPED_BY + devicePoolName)
					.withManufacturer("Clumsy-" + devicePoolName)
					.withModel("Clumsy=" + devicePoolName)
					.build();
	}

	private void compileResultsForDevice(DevicePool devicePool, PoolSummary.Builder poolSummaryBuilder, Device device) {
		String poolName = devicePool.getName();
		File[] deviceResultFiles = getTestResultFiles(poolName, device.getSerial());
        if (deviceResultFiles == null) {
            return;
        }
		for (File file : deviceResultFiles) {
			Collection<TestResult> testResultsForFile = compileTestResultsForFile(file, device, poolName);
			poolSummaryBuilder.addTestResults(testResultsForFile);
		}
	}

	private Collection<TestResult> compileTestResultsForFile(File file, Device device, String poolName) {
		try {
			TestSuite testSuite = serializer.read(TestSuite.class, file, STRICT);
			List<TestCase> testCases = testSuite.getTestCases();
			if ((testCases == null) || testCases.isEmpty()) {
				return new ArrayList<>(0);
			}
			return transform(testCases, toTestResult(device, poolName));
		} catch (Exception e) {
			throw new RuntimeException("Error when parsing file: " + file.getAbsolutePath(), e);
		}
	}

	private Function<TestCase, TestResult> toTestResult(final Device device, final String poolName) {
		return new Function<TestCase, TestResult>() {
			@Override
			@Nullable
			public TestResult apply(@Nullable TestCase testCase) {
				return aTestResult()
						.withPoolName(poolName)
						.withDevice(device)
						.withTestClass(testCase.getClassname())
						.withTestMethod(testCase.getName())
						.withTimeTaken(testCase.getTime())
						.withErrorTrace(testCase.getError())
						.withFailureTrace(testCase.getFailure())
						.build();
			}
		};
	}

	private File[] getTestResultFiles(String poolName, String serial) {
        return fileManager.getTestFilesForDevice(poolName, serial);
	}
}
