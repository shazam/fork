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
package com.shazam.fork;

import com.android.ddmlib.AndroidDebugBridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.injector.ForkRunnerInjector.forkRunner;
import static com.shazam.fork.model.InstrumentationInfo.parseFromFile;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * Represents a collection of devices and the test output to be executed.
 */
public final class Fork {
    private static final Logger logger = LoggerFactory.getLogger(Fork.class);

    private final ForkRunner forkRunner;
    private final File output;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean run() {
		long startOfTests = nanoTime();
		try {
            deleteDirectory(output);
            output.mkdirs();
            return forkRunner.run();
		} catch (Exception e) {
            logger.error("Error while running Fork", e);
			return false;
		} finally {
			AndroidDebugBridge.terminate();
            long elapsedNanos = nanoTime() - startOfTests;
            long durationInMs = MILLISECONDS.convert(elapsedNanos, NANOSECONDS);
            logger.info("Total time taken: {} ms", durationInMs);
		}
	}

	private Fork(Builder builder) {
        Configuration configuration = new Configuration(
                builder.androidSdk,
                builder.applicationApk,
                builder.instrumentationApk,
                parseFromFile(builder.instrumentationApk),
                builder.output,
                builder.idleTimeout,
                builder.testTimeout,
                builder.testIntervalTimeout);

        this.output = configuration.getOutput();
        this.forkRunner = forkRunner(configuration);
    }

    /** Build a test suite for the specified devices and output. */
	public static class Builder {
		private File androidSdk;
		private File applicationApk;
		private File instrumentationApk;
		private File output;
        private int idleTimeout = 2 * 60 * 1000; // Empirical default.
        private int testTimeout = 4 * 60 * 1000; // Empirical default.
        private int testIntervalTimeout = 30 * 1000; // Empirical default.

        public static Builder aFork() {
			return new Builder();
		}

        /**
         * Path to the local Android SDK directory.
         * @param androidSdk android SDK location
         * @return this builder
         */
		public Builder withAndroidSdk(File androidSdk) {
			this.androidSdk = androidSdk;
			return this;
		}

        /**
         * Path to application APK.
         * @param apk the location of the production APK
         * @return this builder
         */
		public Builder withApplicationApk(File apk) {
			applicationApk = apk;
			return this;
		}

        /**
         * Path to the instrumentation APK.
         * @param apk the location of the instrumentation APK
         * @return this builder
         */
		public Builder withInstrumentationApk(File apk) {
			instrumentationApk = apk;
			return this;
		}

        /**
         * Path to output directory.
         * @param output the output directory
         * @return this builder
         */
		public Builder withOutputDirectory(File output) {
			this.output = output;
			return this;
		}

        /**
         * Maximum inactivity of a device
         * @param idleTimeout the period in millis
         * @return this builder
         */
        public Builder withIdleTimeout(int idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        /**
         * Maximum time a test can take
         * @param testTimeout the period in millis
         * @return this builder
         */
        public Builder withTestTimeout(int testTimeout) {
            this.testTimeout = testTimeout;
            return this;
        }

        /**
         * Millis for maximum time between two tests
         * @param testIntervalTimeout the period in millis
         * @return this builder
         */
        public Builder withTestIntervalTimeout(int testIntervalTimeout) {
            this.testIntervalTimeout = testIntervalTimeout;
            return this;
        }

        public Fork build() {
			checkNotNull(androidSdk, "SDK is required.");
			checkArgument(androidSdk.exists(), "SDK directory does not exist.");
			checkNotNull(applicationApk, "Application APK is required.");
			checkArgument(applicationApk.exists(), "Application APK file does not exist.");
			checkNotNull(instrumentationApk, "Instrumentation APK is required.");
			checkArgument(instrumentationApk.exists(), "Instrumentation APK file does not exist.");
			checkNotNull(output, "Output path is required.");

			return new Fork(this);
		}
    }
}
