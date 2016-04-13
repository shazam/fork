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

import com.google.common.collect.Lists;
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.PoolTestRunner;
import com.shazam.fork.system.io.FileManager;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collection;
import java.util.List;

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
            PoolSummary poolSummary = compilePoolSummary(pool, summaryBuilder);
            summaryBuilder.addPoolSummary(poolSummary);
        }
        addIgnoredTests(testCases, summaryBuilder);
        summaryBuilder.withTitle(runtimeConfiguration.getTitle());
        summaryBuilder.withSubtitle(runtimeConfiguration.getSubtitle());

        return summaryBuilder.build();
    }

    private PoolSummary compilePoolSummary(Pool pool, Summary.Builder summaryBuilder) {
        PoolSummary.Builder poolSummaryBuilder = aPoolSummary().withPoolName(pool.getName());
        for (Device device : pool.getDevices()) {
            compileResultsForDevice(pool, poolSummaryBuilder, summaryBuilder, device);
        }
        Device watchdog = getPoolWatchdog(pool.getName());
        compileResultsForDevice(pool, poolSummaryBuilder, summaryBuilder, watchdog);
        return poolSummaryBuilder.build();
    }

    private void compileResultsForDevice(Pool pool, PoolSummary.Builder poolSummaryBuilder, Summary.Builder summaryBuilder, Device device) {
        File[] deviceResultFiles = fileManager.getTestFilesForDevice(pool, device);
        if (deviceResultFiles == null) {
            return;
        }
        for (File file : deviceResultFiles) {
            Collection<TestResult> testResult = parseTestResultsFromFile(file, device);
            poolSummaryBuilder.addTestResults(testResult);
            addFailedTests(testResult, summaryBuilder);
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

    private void addFailedTests(Collection<TestResult> testResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : testResults) {
            int totalFailureCount = testResult.getTotalFailureCount();
            if (totalFailureCount > 0) {
                String failedTest = totalFailureCount + " times " + testResult.getTestClass()
                        + "#" + testResult.getTestMethod() + " on " + testResult.getDevice().getSerial() ;
                summaryBuilder.addFailedTests(failedTest);
            }
        }
    }

    private Collection<TestResult> parseTestResultsFromFile(File file, Device device) {
        try {
            TestSuite testSuite = serializer.read(TestSuite.class, file, STRICT);
            Collection<TestCase> testCases = testSuite.getTestCase();
            List<TestResult> result  = Lists.newArrayList();
            if ((testCases == null)) {
                return result;
            }

            for(TestCase testCase : testCases){
                TestResult testResult = getTestResult(device, testSuite, testCase);
                result.add(testResult);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error when parsing file: " + file.getAbsolutePath(), e);
        }
    }

    private TestResult getTestResult(Device device, TestSuite testSuite, TestCase testCase) {
        TestResult.Builder testResultBuilder = aTestResult()
                .withDevice(device)
                .withTestClass(testCase.getClassname())
                .withTestMethod(testCase.getName())
                .withTimeTaken(testCase.getTime())
                .withErrorTrace(testCase.getError())
                .withFailureTrace(testCase.getFailure());
        if (testSuite.getProperties() != null) {
            testResultBuilder.withTestMetrics(testSuite.getProperties());
        }
        return testResultBuilder.build();
    }
}
