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

import static com.shazam.fork.Configuration.Builder.configuration;
import static com.shazam.fork.injector.GsonInjector.gson;
import static com.shazam.fork.utils.Utils.cleanFile;

public class ForkCli {

    private static final Logger logger = LoggerFactory.getLogger(ForkCli.class);

    private ForkCli() {}

    public static class CommandLineArgs {

        @Parameter(names = { "--sdk" }, description = "Path to Android SDK", converter = FileConverter.class)
        public File sdk;

        @Parameter(names = { "--apk" }, description = "Application APK", converter = FileConverter.class,
                required = true)
        public File apk;

        @Parameter(names = { "--test-apk" }, description = "Test application APK", converter = FileConverter.class,
                required = true)
        public File testApk;

        @Parameter(names = { "--config" }, description = "Path of JSON config file", converter = FileConverter.class,
                required = true)
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
            ForkConfiguration forkConfiguration = gson().fromJson(configFileReader, ForkConfiguration.class);

            Configuration configuration = configuration()
                    .withAndroidSdk(parsedArgs.sdk != null ? parsedArgs.sdk : cleanFile(CommonDefaults.ANDROID_SDK))
                    .withApplicationApk(parsedArgs.apk)
                    .withInstrumentationApk(parsedArgs.testApk)
                    .withOutput(forkConfiguration.baseOutputDir != null ? cleanFile(forkConfiguration.baseOutputDir) : cleanFile(Defaults.FORK_OUTPUT))
                    .withTitle(forkConfiguration.title)
                    .withSubtitle(forkConfiguration.subtitle)
                    .withTestClassRegex(forkConfiguration.testClassRegex)
                    .withTestPackage(forkConfiguration.testPackage)
                    .withTestOutputTimeout(forkConfiguration.testOutputTimeout)
                    .withTestSize(forkConfiguration.testSize)
                    .withExcludedSerials(forkConfiguration.excludedSerials)
                    .withFallbackToScreenshots(forkConfiguration.fallbackToScreenshots)
                    .withTotalAllowedRetryQuota(forkConfiguration.totalAllowedRetryQuota)
                    .withRetryPerTestCaseQuota(forkConfiguration.retryPerTestCaseQuota)
                    .withCoverageEnabled(forkConfiguration.isCoverageEnabled)
                    .withPoolingStrategy(forkConfiguration.poolingStrategy)
                    .withAutoGrantPermissions(forkConfiguration.autoGrantPermissions)
                    .withRestartAdbIfNoDevices(forkConfiguration.restartAdbIfNoDevices)
                    .build();

            Fork fork = new Fork(configuration);
            if (!fork.run() && !forkConfiguration.ignoreFailures) {
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find configuration file", e);
            System.exit(1);
        }
    }
}
