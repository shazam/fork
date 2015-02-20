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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;

import static com.shazam.fork.ForkBuilder.aFork;

public class ForkCli {

    private static final Logger logger = LoggerFactory.getLogger(ForkCli.class);

    public static class CommandLineArgs {

        @Parameter(names = { "--sdk" }, description = "Path to Android SDK")
        public File sdk = cleanFile(System.getenv("ANDROID_HOME"));

        @Parameter(names = { "--apk" }, description = "Application APK", converter = FileConverter.class,
                required = true)
        public File apk;

        @Parameter(names = { "--test-apk" }, description = "Test application APK", converter = FileConverter.class,
                required = true)
        public File testApk;

        @Parameter(names = { "--output" }, description = "Output path", converter = FileConverter.class)
        public File output;

        @Parameter(names = { "--testClassPattern" }, description = "Regex determining class names to consider when finding tests to run", converter = PatternConverter.class)
        public Pattern testClassPattern;

        @Parameter(names = { "--testPackagePattern" }, description = "Regex determining packages to consider when finding tests to run. " +
                "Defaults to the instrumentation package.", converter = PatternConverter.class)
        public Pattern testPackagePattern;

        @Parameter(names = { "--fail-on-failure" }, description = "Non-zero exit code on failure")
        public boolean failOnFailure;

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

    @SuppressWarnings("ReturnOfNull")
    private static File cleanFile(String path) {
        if (path == null) {
            return null;
        }
        return new File(path);
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

        Fork fork = aFork()
                .withApplicationApk(parsedArgs.apk)
                .withInstrumentationApk(parsedArgs.testApk)
                .withOutputDirectory(parsedArgs.output)
                .withAndroidSdk(parsedArgs.sdk)
                .withTestClassPattern(parsedArgs.testClassPattern)
                .withTestPackagePattern(parsedArgs.testPackagePattern)
                .build();

        if (!fork.run() && parsedArgs.failOnFailure) {
            System.exit(1);
        }
    }
}
