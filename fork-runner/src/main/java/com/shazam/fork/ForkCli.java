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

import com.beust.jcommander.*;
import com.beust.jcommander.converters.IntegerConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.shazam.fork.ForkBuilder.aFork;
import static com.shazam.fork.Utils.cleanFile;

public class ForkCli {

    private static final Logger logger = LoggerFactory.getLogger(ForkCli.class);

    public static class CommandLineArgs {

        @Parameter(names = { "--sdk" }, description = "Path to Android SDK", converter = FileConverter.class)
        public File sdk;

        @Parameter(names = { "--apk" }, description = "Application APK", converter = FileConverter.class,
                required = true)
        public File apk;

        @Parameter(names = { "--test-apk" }, description = "Test application APK", converter = FileConverter.class,
                required = true)
        public File testApk;

        @Parameter(names = { "--output" }, description = "Output path", converter = FileConverter.class)
        public File output;

        @Parameter(names = { "--test-class-regex" }, description = "Regex determining class names to consider when finding tests to run")
        public String testClassRegex;

        @Parameter(names = { "--test-package" }, description = "The package where your tests are located. " +
                "Defaults to the instrumentation package.")
        public String testPackage;

        @Parameter(names = { "--test-timeout" }, description = "The maximum amount of time during which the tests are " +
                "allowed to not output any response, in milliseconds", converter = IntegerConverter.class)
        public int testOutputTimeout = -1;

        @Parameter(names = { "--fallback-to-screenshots" }, description = "Allowed to fallback to screenshots when video" +
                " recording is not supported" , arity = 1)
        public boolean fallbackToScreenshots = true;

        @Parameter(names = { "--fail-on-failure" }, description = "Non-zero exit code on failure")
        public boolean failOnFailure = true;

        @Parameter(names = { "-h", "--help" }, description = "Command help", help = true, hidden = true)
        public boolean help;

        @Parameter(names = { "--total-allowed-retry-quota" }, description = "Amount of re-executions of failing tests allowed.", converter = IntegerConverter.class)
        public int totalAllowedRetryQuota = 0;

       @Parameter(names = { "--retry-per-test-case-quota" }, description = "Max number of time each testCase is attempted again " +
                "before declaring it as a failure.", converter = IntegerConverter.class)
        public int retryPerTestCaseQuota = 1;

    }

    /* JCommander deems it necessary that this class be public. Lame. */
    public static class FileConverter implements IStringConverter<File> {

        @Override
        public File convert(String s) {
            return cleanFile(s);
        }
    }

    public static void main(String... args) {
        CommandLineArgs parsedArgs = new CommandLineArgs();
        JCommander jc = new JCommander(parsedArgs);

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            StringBuilder out = new StringBuilder(e.getLocalizedMessage()).append("\n\n");
            jc.usage(out);
            logger.error(out.toString());
            System.exit(1);
            return;
        }
        if (parsedArgs.help) {
            jc.usage();
            return;
        }

        ForkBuilder forkBuilder = aFork()
                .withApplicationApk(parsedArgs.apk)
                .withInstrumentationApk(parsedArgs.testApk)
                .withFallbackToScreenshots(parsedArgs.fallbackToScreenshots);

        overrideDefaultsIfSet(forkBuilder, parsedArgs);

        Fork fork = forkBuilder.build();
        if (!fork.run() && parsedArgs.failOnFailure) {
            System.exit(1);
        }
    }

    private static void overrideDefaultsIfSet(ForkBuilder forkBuilder, CommandLineArgs parsedArgs) {
        if (parsedArgs.sdk != null) {
            forkBuilder.withAndroidSdk(parsedArgs.sdk);
        }

        if (parsedArgs.output != null) {
            forkBuilder.withOutputDirectory(parsedArgs.output);
        }

        if (parsedArgs.testClassRegex != null) {
            forkBuilder.withTestClassRegex(parsedArgs.testClassRegex);
        }

        if (parsedArgs.testPackage != null) {
            forkBuilder.withTestPackage(parsedArgs.testPackage);
        }

        if (parsedArgs.testOutputTimeout > -1) {
            forkBuilder.withTestOutputTimeout(parsedArgs.testOutputTimeout);
        }

        if (parsedArgs.totalAllowedRetryQuota > 0) {
            forkBuilder.withTotalAllowedRetryQuota(parsedArgs.totalAllowedRetryQuota);
        }

        if(parsedArgs.retryPerTestCaseQuota > -1){
            forkBuilder.withRetryPerTestCaseQuota(parsedArgs.retryPerTestCaseQuota);
        }
    }
}
