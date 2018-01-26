package com.shazam.fork.util;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;

import static java.util.Collections.emptyMap;

public class TestPipelineEmulator {
    private final String trace;
    private final String fatalErrorMessage;

    private TestPipelineEmulator(Builder builder) {
        this.trace = builder.trace;
        this.fatalErrorMessage = builder.fatalErrorMessage;
    }

    public void emulateFor(ITestRunListener testRunListener, TestIdentifier test) {
        testRunListener.testRunStarted("emulated", 1);
        testRunListener.testStarted(test);
        if (trace != null) {
            testRunListener.testFailed(test, trace);
        }
        testRunListener.testEnded(test, emptyMap());
        if (fatalErrorMessage != null) {
            testRunListener.testRunFailed(fatalErrorMessage);
        }
        testRunListener.testRunEnded(100L, emptyMap());
    }

    public static class Builder {
        private String trace;
        private String fatalErrorMessage;

        public static Builder testPipelineEmulator() {
            return new Builder();
        }

        public Builder withTrace(String trace) {
            this.trace = trace;
            return this;
        }

        public Builder withFatalErrorMessage(String fatalErrorMessage) {
            this.fatalErrorMessage = fatalErrorMessage;
            return this;
        }

        public TestPipelineEmulator build() {
            return new TestPipelineEmulator(this);
        }
    }
}
