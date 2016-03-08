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
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.*;
import com.shazam.fork.runner.PoolTestRunner;
import com.shazam.fork.system.io.FileManager;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.*;

import javax.annotation.Nonnull;

import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;

public class SummaryCompiler {

    private static final boolean STRICT = false;

    private final RuntimeConfiguration runtimeConfiguration;
    private final FileManager fileManager;
    private final Serializer serializer;

    public SummaryCompiler(RuntimeConfiguration runtimeConfiguration, FileManager fileManager) {
        this.runtimeConfiguration = runtimeConfiguration;
        this.fileManager = fileManager;
		serializer = new Persister();
	}

	Summary compileSummary(Collection<Pool> pools, List<TestCaseEvent> testCases) {
		Summary.Builder summaryBuilder = aSummary();
		for (Pool pool : pools) {
            PoolSummary poolSummary = compilePoolSummary(pool);
            summaryBuilder.addPoolSummary(poolSummary);
		}
        addIgnoredTests(testCases, summaryBuilder);
        summaryBuilder.withTitle(runtimeConfiguration.getTitle());
        summaryBuilder.withSubtitle(runtimeConfiguration.getSubtitle());

		return summaryBuilder.build();
	}

    private PoolSummary compilePoolSummary(Pool pool) {
        PoolSummary.Builder poolSummaryBuilder = aPoolSummary().withPoolName(pool.getName());
        for (Device device: pool.getDevices()) {
            compileResultsForDevice(pool, poolSummaryBuilder, device);
        }
        Device watchdog = getPoolWatchdog(pool.getName());
        compileResultsForDevice(pool, poolSummaryBuilder, watchdog);
        return poolSummaryBuilder.build();
    }

    private void compileResultsForDevice(Pool pool, PoolSummary.Builder poolSummaryBuilder, Device device) {
        File[] deviceResultFiles = fileManager.getTestFilesForDevice(pool, device);
        if (deviceResultFiles == null) {
            return;
        }
        for (File file : deviceResultFiles) {
            Collection<TestResult> testResults = parseTestResultsFromFile(file, device);
            poolSummaryBuilder.addTestResults(testResults);
        }
    }

    private Device getPoolWatchdog(String poolName) {
        return aDevice()
                .withSerial(PoolTestRunner.DROPPED_BY + poolName)
                .withManufacturer("Clumsy-" + poolName)
                .withModel("Clumsy=" + poolName)
                .build();
    }

    private void addIgnoredTests(List<TestCaseEvent> testCases, Summary.Builder summaryBuilder) {
        for (TestCaseEvent testCase : testCases) {
            if (testCase.isIgnored()) {
                summaryBuilder.addIgnoredTest(testCase.getTestClass() + ":" + testCase.getTestMethod());
            }
        }
    }

	private Collection<TestResult> parseTestResultsFromFile(File file, Device device) {
		try {
			TestSuite testSuite = serializer.read(TestSuite.class, file, STRICT);
			List<TestCase> testCases = testSuite.getTestCases();
			if ((testCases == null) || testCases.isEmpty()) {
				return new ArrayList<>(0);
			}
			return transform(testCases, toTestResult(device));
		} catch (Exception e) {
			throw new RuntimeException("Error when parsing file: " + file.getAbsolutePath(), e);
		}
	}

	private Function<TestCase, TestResult> toTestResult(final Device device) {
		return new Function<TestCase, TestResult>() {
			@SuppressWarnings("NullableProblems")
            @Override
			public TestResult apply(@Nonnull TestCase testCase) {
				return aTestResult()
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
}
