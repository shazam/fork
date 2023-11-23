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
package com.shazam.fork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.shazam.fork.injector.ConfigurationInjector.setConfiguration;
import static com.shazam.fork.injector.ForkRunnerInjector.forkRunner;
import static com.shazam.fork.injector.system.AdbInjector.releaseAdb;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatPeriod;

public final class Fork {
    private static final Logger logger = LoggerFactory.getLogger(Fork.class);

    private final ForkRunner forkRunner;
    private final File output;

    public Fork(Configuration configuration) {
        this.output = configuration.getOutput();
        setConfiguration(configuration);
        this.forkRunner = forkRunner();
    }

    public boolean run() {
		long startOfTestsMs = nanoTime();
		try {
            deleteDirectory(output);
            //noinspection ResultOfMethodCallIgnored
            output.mkdirs();
            return forkRunner.run();
		} catch (Exception e) {
            logger.error("Error while running Fork", e);
			return false;
		} finally {
            long duration = millisSinceNanoTime(startOfTestsMs);
            logger.info(formatPeriod(0, duration, "'Total time taken:' H 'hours' m 'minutes' s 'seconds'"));
            releaseAdb();
		}
	}
}
