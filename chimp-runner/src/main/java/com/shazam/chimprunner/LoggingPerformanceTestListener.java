/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;

class LoggingPerformanceTestListener extends NoOpPerformanceTestListener {
    private static final Logger logger = LoggerFactory.getLogger(LoggingPerformanceTestListener.class);

    private final String testClassName;
    private final String testMethodName;
    private long iterationStartTime;
    private int count;
    private final Collection<Long> timings;

    public LoggingPerformanceTestListener(String testClassName, String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        timings = new ArrayList<>();
    }

    @Override
    public void startOverall() {
        logger.info("Starting test {} {}", testClassName, testMethodName);
    }

    @Override
    public void startIteration() {
        count++;
        iterationStartTime = nanoTime();
    }

    @Override
    public void endIteration() {
        if (count > Defaults.ITERATIONS_TO_SKIP) {
            long iterationDuration = millisSinceNanoTime(iterationStartTime);
            logger.debug("Iteration time: " + iterationDuration);
            timings.add(iterationDuration);
        } else {
            logger.debug("Skipping iteration " + count);
        }
    }

    @Override
    public void endOverall() {
        OptionalDouble average = timings.stream()
                .mapToDouble(t -> t)
                .average();
        logger.info("Average time: " + average.getAsDouble());
    }
}