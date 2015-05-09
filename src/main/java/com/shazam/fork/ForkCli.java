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
import java.util.regex.Pattern;

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

        @Parameter(names = { "--test-class-pattern" }, description = "Regex determining class names to consider when finding tests to run",
                converter = PatternConverter.class)
        public Pattern testClassPattern;

        @Parameter(names = { "--test-package-pattern" }, description = "Regex determining packages to consider when finding tests to run. " +
                "Defaults to the instrumentation package.", converter = PatternConverter.class)
        public Pattern testPackagePattern;

        @Parameter(names = { "--test-timeout" }, description = "The maximum amount of time during which the tests are " +
                "allowed to not output any response, in milliseconds", converter = IntegerConverter.class)
        public int testOutputTimeout = -1;

        @Parameter(names = { "--fail-on-failure" }, description = "Non-zero exit code on failure")
        public boolean failOnFailure = true;

        @Parameter(names = { "-h", "--help" }, description = "Command help", help = true, hidden = true)
        public boolean help;
    }

    /* JCommander deems it necessary that this class be public. Lame. */
    public static class FileConverter implements IStringConverter<File> {

        @Override
        public File convert(String s) {
            return cleanFile(s);
        }
    }

    public static class PatternConverter implements IStringConverter<Pattern> {

        @Override
        public Pattern convert(String string) {
            return Pattern.compile(string);
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
                .withInstrumentationApk(parsedArgs.testApk);

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

        if (parsedArgs.testClassPattern != null) {
            forkBuilder.withTestClassPattern(parsedArgs.testClassPattern);
        }

        if (parsedArgs.testPackagePattern != null) {
            forkBuilder.withTestPackagePattern(parsedArgs.testPackagePattern);
        }

        if (parsedArgs.testOutputTimeout > -1) {
            forkBuilder.withTestOutputTimeout(parsedArgs.testOutputTimeout);
        }
    }
}
