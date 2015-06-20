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

import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.*;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SummaryGeneratorHook extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SummaryGeneratorHook.class);

    private final AtomicBoolean hasNotRunYet = new AtomicBoolean(true);
    private final RuntimeConfiguration runtimeConfiguration;
    private final FileManager fileManager;
    private final Collection<Pool> pools;
    private final List<TestClass> testClasses;
    private final SummaryPrinter summaryPrinter;

    public SummaryGeneratorHook(RuntimeConfiguration runtimeConfiguration,
                                FileManager fileManager,
                                Collection<Pool> pools,
                                List<TestClass> testClasses,
                                SummaryPrinter summaryPrinter) {
        this.runtimeConfiguration = runtimeConfiguration;
        this.fileManager = fileManager;
        this.pools = pools;
		this.testClasses = testClasses;
        this.summaryPrinter = summaryPrinter;
    }

	@Override
	public void run() {
		if (hasNotRunYet.get()) {
			logger.info("************************************************************************************");
			logger.info("************************** SUMMARY GENERATION SHUTDOWN HOOK ************************");
			logger.info("************************************************************************************");
			defineOutcome();
		}
	}

    /**
     * This only gets executed once, but needs to check the flag in case it finished normally and then shutdown.
     *
     * @return a result summary
     */
	public boolean defineOutcome() {
        if (hasNotRunYet.compareAndSet(true, false)) {
			Summarizer summarizer = new Summarizer(runtimeConfiguration, fileManager, pools,
                    testClasses);
			Summary summary = summarizer.compileSummary();
            summaryPrinter.print(summary);
            return summary != null && new OutcomeAggregator().aggregate(summary);
		}
		return false;
	}
}
