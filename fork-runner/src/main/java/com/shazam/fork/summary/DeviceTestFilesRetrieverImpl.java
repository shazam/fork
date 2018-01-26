package com.shazam.fork.summary;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.io.FileManager;
import org.simpleframework.xml.Serializer;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.shazam.fork.summary.TestResult.Builder.aTestResult;
import static java.util.Collections.emptyList;

public class DeviceTestFilesRetrieverImpl implements DeviceTestFilesRetriever {
    private static final boolean STRICT = false;
    private final FileManager fileManager;
    private final Serializer serializer;

    public DeviceTestFilesRetrieverImpl(FileManager fileManager, Serializer serializer) {
        this.fileManager = fileManager;
        this.serializer = serializer;
    }

    @Nonnull
    @Override
    public Collection<TestResult> getTestResultsForDevice(Pool pool, Device device) {
        File[] deviceResultFiles = fileManager.getTestFilesForDevice(pool, device);
        if (deviceResultFiles == null) {
            return emptyList();
        }
        return parseTestResultsFromFiles(deviceResultFiles, device);
    }

    private Collection<TestResult> parseTestResultsFromFiles(File[] deviceResultFiles, Device device) {
        Set<TestResult> testResults = Sets.newHashSet();
        for (File deviceResultFile : deviceResultFiles) {
            testResults.addAll(parseTestResultsFromFile(deviceResultFile, device));
        }
        return testResults;
    }

    private Collection<TestResult> parseTestResultsFromFile(File file, Device device) {
        try {
            TestSuite testSuite = serializer.read(TestSuite.class, file, STRICT);
            Collection<TestCase> testCases = testSuite.getTestCase();
            List<TestResult> result = Lists.newArrayList();
            if ((testCases == null)) {
                return result;
            }

            for (TestCase testCase : testCases) {
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
