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

import java.io.*;
import java.util.regex.Pattern;

import static com.shazam.fork.Configuration.Builder.configuration;
import static com.shazam.fork.Utils.cleanFile;
import static com.shazam.fork.injector.GsonInjector.gson;

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

        @Parameter(names = { "--config" }, description = "Path of JSON config file", converter = FileConverter.class)
        public File configurationFile;

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

        try {
            Reader configFileReader = new FileReader(parsedArgs.configurationFile);
            UserConfiguration userConfiguration = gson().fromJson(configFileReader, UserConfiguration.class);

            Configuration.Builder configurationBuilder = configuration()
                    .withAndroidSdk(parsedArgs.sdk != null ? parsedArgs.sdk : cleanFile(Defaults.ANDROID_SDK))
                    .withApplicationApk(parsedArgs.apk)
                    .withInstrumentationApk(parsedArgs.testApk)
                    .withOutput(userConfiguration.baseOutputDir != null ? cleanFile(userConfiguration.baseOutputDir) : cleanFile(Defaults.TEST_OUTPUT))
                    .withTestPackage(userConfiguration.testPackage)
                    .withFallbackToScreenshots(userConfiguration.fallbackToScreenshots)
                    .withCoverageEnabled(userConfiguration.isCoverageEnabled)
                    .withPoolingStrategy(userConfiguration.poolingStrategy);
            overrideDefaultsIfSet(configurationBuilder, userConfiguration);

            Fork fork = new Fork(configurationBuilder.build());
            if (!fork.run() && !userConfiguration.ignoreFailures) {
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find configuration file", e);
            System.exit(1);
        }
    }

    /**
     * Only interested in values that have defaults in Configuration.Builder
     */
    private static void overrideDefaultsIfSet(Configuration.Builder configurationBuilder, UserConfiguration userConfiguration) {
        if (userConfiguration.title != null) {
            configurationBuilder.withTitle(userConfiguration.title);
        }

        if (userConfiguration.subtitle != null) {
            configurationBuilder.withSubtitle(userConfiguration.subtitle);
        }

        if (userConfiguration.testClassRegex != null) {
            configurationBuilder.withTestClassPattern(Pattern.compile(userConfiguration.testClassRegex));
        }

        if (userConfiguration.testOutputTimeout > 0) {
            configurationBuilder.withTestOutputTimeout(userConfiguration.testOutputTimeout);
        }

        if (userConfiguration.testSize != null) {
            configurationBuilder.withTestSize(userConfiguration.testSize);
        }

        if (userConfiguration.excludedSerials != null) {
            configurationBuilder.withExcludedSerials(userConfiguration.excludedSerials);
        }

        if (userConfiguration.totalAllowedRetryQuota > 0) {
            configurationBuilder.withTotalAllowedRetryQuota(userConfiguration.totalAllowedRetryQuota);
        }

        if(userConfiguration.retryPerTestCaseQuota > 0) {
            configurationBuilder.withRetryPerTestCaseQuota(userConfiguration.retryPerTestCaseQuota);
        }
    }
}
