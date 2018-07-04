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

import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Making this a shutdown hook, to generate reports even when the VM is killed. This may also be called normally
 * by the program execution, in which case we don't re-generate the reports.
 */
public class SummaryGeneratorHook extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SummaryGeneratorHook.class);

    private final AtomicBoolean hasNotRunYet = new AtomicBoolean(true);
    private final Summarizer summarizer;

    private Collection<Pool> pools;
    private Collection<TestCaseEvent> testCases;

    public SummaryGeneratorHook(Summarizer summarizer) {
        this.summarizer = summarizer;
    }

    /**
     * Sets the pools and test classes for which a summary will be created either at normal execution or as a
     * shutdown hook.
     *
     * @param pools     the pools to consider for the summary
     * @param testCases the test cases for the summary
     */
    public void registerHook(Collection<Pool> pools, Collection<TestCaseEvent> testCases) {
        this.pools = pools;
        this.testCases = testCases;
        Runtime.getRuntime().addShutdownHook(this);
    }

    /**
     * This only gets executed once, but needs to check the flag in case it finished normally and then shutdown.
     * It can only be called after {@link SummaryGeneratorHook#registerHook(Collection, Collection)}.
     *
     * @return <code>true</code> - if tests have passed
     */
    public void generateSummary(boolean isSuccessful) {
        if (hasNotRunYet.compareAndSet(true, false)) {
            summarizer.summarize(isSuccessful, pools, testCases);
        }
    }

    @Override
    public void run() {
        if (hasNotRunYet.get()) {
            logger.info("************************************************************************************");
            logger.info("************************** SUMMARY GENERATION SHUTDOWN HOOK ************************");
            logger.info("************************************************************************************");
            generateSummary(false);
        }
    }
}
