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

import com.shazam.fork.model.Device;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.shazam.fork.summary.ResultStatus.ERROR;
import static com.shazam.fork.summary.ResultStatus.FAIL;
import static com.shazam.fork.summary.ResultStatus.IGNORED;
import static com.shazam.fork.summary.ResultStatus.PASS;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.hash;

public class TestResult {
    public static final String SUMMARY_KEY_TOTAL_FAILURE_COUNT = "totalFailureCount";

    private final Device device;
    private final float timeTaken;
    private final String testClass;
    private final String testMethod;
    private final String errorTrace;
    private final String failureTrace;
    private final Map<String, String> testMetrics;
    private final boolean isIgnored;

    public Device getDevice() {
        return device;
    }

    public float getTimeTaken() {
        return timeTaken;
    }

    public String getTestFullName() {
        return testClass + "#" + testMethod;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public int getTotalFailureCount() {
        int result = 0;

        if (testMetrics != null
                && testMetrics.containsKey(SUMMARY_KEY_TOTAL_FAILURE_COUNT)) {
            result = Integer.parseInt(testMetrics.get(SUMMARY_KEY_TOTAL_FAILURE_COUNT));
        }
        return result;
    }

    @Nonnull
    public ResultStatus getResultStatus() {
        if (isIgnored) {
            return IGNORED;
        }
        if (!isNullOrEmpty(errorTrace)) {
            return ERROR;
        }
        if (!isNullOrEmpty(failureTrace)) {
            return FAIL;
        }
        return PASS;
    }

    public String getTrace() {
        switch (getResultStatus()) {
            case ERROR:
                return errorTrace;
            case FAIL:
                return failureTrace;
            default:
                return "";
        }
    }

    public String getDeviceSerial() {
        if (device != null) {
            return device.getSerial();
        }
        return "Unknown device";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return Objects.equals(device, that.device) &&
                Objects.equals(testClass, that.testClass) &&
                Objects.equals(testMethod, that.testMethod);
    }

    @Override
    public int hashCode() {
        return hash(device, testClass, testMethod);
    }

    @Override
    public String toString() {
        return format(ENGLISH, "TestResult{test=%s,device=%s,time=%f}", getTestFullName(), device, timeTaken);
    }

    public static class Builder {
        private Device device;
        private float timeTaken;
        private String testClass;
        private String testMethod;
        private String errorTrace;
        private String failureTrace;
        private boolean isIgnored;
        private Map<String, String> testMetrics = new HashMap<>();

        public static Builder aTestResult() {
            return new Builder();
        }

        public Builder withDevice(Device device) {
            this.device = device;
            return this;
        }

        public Builder withTimeTaken(float timeTaken) {
            this.timeTaken = timeTaken;
            return this;
        }

        public Builder withTestClass(String testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder withTestMethod(String testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        public Builder withErrorTrace(String trace) {
            if (!isNullOrEmpty(trace)) {
                errorTrace = trace;
            }
            return this;
        }

        public Builder withFailureTrace(String trace) {
            if (!isNullOrEmpty(trace)) {
                failureTrace = trace;
            }
            return this;
        }

        public Builder withIgnored(boolean isIgnored) {
            this.isIgnored = isIgnored;
            return this;
        }

        public Builder withTestMetrics(Map<String, String> testMetrics) {
            this.testMetrics.clear();
            this.testMetrics.putAll(testMetrics);
            return this;
        }

        public TestResult build() {
            return new TestResult(this);
        }

    }

    private TestResult(Builder builder) {
        device = builder.device;
        timeTaken = builder.timeTaken;
        testClass = builder.testClass;
        testMethod = builder.testMethod;
        errorTrace = builder.errorTrace;
        failureTrace = builder.failureTrace;
        testMetrics = builder.testMetrics;
        isIgnored = builder.isIgnored;
    }
}
