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

import com.shazam.fork.Configuration;
import com.shazam.fork.RuntimeConfiguration;
import com.shazam.fork.model.DevicePool;
import com.shazam.fork.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ReportGeneratorHook extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorHook.class);

    private final Configuration configuration;
    private final RuntimeConfiguration runtimeConfiguration;
    private final Collection<DevicePool> devicePools;
    private final List<TestClass> testClasses;
    private final SummaryPrinter summaryPrinter;

    public ReportGeneratorHook(Configuration configuration, RuntimeConfiguration runtimeConfiguration,
                               Collection<DevicePool> devicePools, List<TestClass> testClasses, SummaryPrinter summaryPrinter) {
        this.configuration = configuration;
        this.runtimeConfiguration = runtimeConfiguration;
        this.devicePools = devicePools;
		this.testClasses = testClasses;
        this.summaryPrinter = summaryPrinter;
    }
	
	@Override
	public void run() {
		if (onlyOnce) {
			logger.info("************************************************************************************");
			logger.info("************************** REPORT GENERATION SHUTDOWN HOOK *************************");
			logger.info("************************************************************************************");
			generateReportOnlyOnce();
		}
	}

	private static boolean onlyOnce = true;

    /**
     * This only gets executed once, but needs to check the flag, in case it finished normally and then shutdown.
     * @return a result summary
     */
    @Nullable
	public synchronized Summary generateReportOnlyOnce() {
		if (onlyOnce) {
			onlyOnce = false;
			Summarizer summarizer = new Summarizer(configuration, runtimeConfiguration, devicePools, testClasses);
			Summary summary = summarizer.compileSummary();
            summaryPrinter.print(summary);
			return summary;
		}
		return null;
	}
}
