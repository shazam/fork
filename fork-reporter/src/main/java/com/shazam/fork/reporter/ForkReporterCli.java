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

import com.beust.jcommander.*;
import com.beust.jcommander.converters.FileConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.shazam.fork.reporter.ForkReporter.Builder.forkReporter;

public class ForkReporterCli {
    private static final Logger logger = LoggerFactory.getLogger(ForkReporterCli.class);

    private ForkReporterCli() {}

    public static void main(String... args) {
        CommandLineArgs parsedArgs = new CommandLineArgs();
        JCommander jc = new JCommander(parsedArgs);

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            StringBuilder out = new StringBuilder(e.getLocalizedMessage()).append("\n\n");
            jc.usage(out);
            logger.error(out.toString());
            jc.usage(out);
            System.exit(1);
            return;
        }
        
        if (parsedArgs.help) {
            jc.usage();
        }

        ForkReporter forkReporter = forkReporter()
                .withInput(parsedArgs.input)
                .withOutput(parsedArgs.output)
                .withTitle(parsedArgs.title)
                .withBaseUrl(parsedArgs.baseUrl)
                .build();
        forkReporter.createReport();

    }

    public static class CommandLineArgs {
        @Parameter(names = { "--input" }, description = "A folder where all of the *.json summary files are located",
                converter = FileConverter.class, required = true)
        public File input;

        @Parameter(names = { "--output" }, description = "The folder where all of the HTML reports will be saved",
                converter = FileConverter.class, required = true)
        public File output;

        @Parameter(names = { "--title" }, description = "The title of the report", required = false)
        public String title;

        @Parameter(names = { "--baseUrl" }, description = "The base URL where the report will be pointing to. " +
                "This URL is templated so needs to contain a {BUILD_ID} token for linking to the correct build. " +
                "The location it points to should contain an /html folder that fork creates at each execution." +
                "E.g. http://build-server.com/master/{BUILD_ID}/fork/", required = false)
        public String baseUrl;

        @Parameter(names = { "-h", "--help" }, description = "Command help", help = true, hidden = true)
        public boolean help;
    }
}
