/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter;

import java.io.File;

import static com.shazam.fork.reporter.injector.ConfigurationInjector.setConfiguration;
import static com.shazam.fork.reporter.injector.ExecutionReaderInjector.executionReader;
import static com.shazam.fork.reporter.injector.FlakinessCalculatorInjector.flakinessCalculator;
import static com.shazam.fork.reporter.injector.FlakinessReportPrinterInjector.flakinessReportPrinter;

public class ForkReporter {
    private final ExecutionReader reader;
    private FlakinessCalculator flakinessCalculator;
    private FlakinessReportPrinter flakinessReportPrinter;

    private ForkReporter(Configuration configuration) {
        setConfiguration(configuration);
        reader = executionReader();
        flakinessCalculator = flakinessCalculator();
        flakinessReportPrinter = flakinessReportPrinter();
    }

    public void createReport() {
        Executions executions = reader.readExecutions();
        FlakinessReport flakinessReport = flakinessCalculator.calculate(executions);
        flakinessReportPrinter.printReport(flakinessReport);
    }

    public static class Builder {
        private File input;
        private File output;

        public static Builder forkReporter() {
            return new Builder();
        }

        public Builder withInput(File input) {
            this.input = input;
            return this;
        }

        public Builder withOutput(File output) {
            this.output = output;
            return this;
        }

        public ForkReporter build() {
            Configuration configuration = new Configuration(input, output);
            return new ForkReporter(configuration);
        }
    }
}
