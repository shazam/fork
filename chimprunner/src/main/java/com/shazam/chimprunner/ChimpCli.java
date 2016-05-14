/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner;

import com.beust.jcommander.*;
import com.shazam.fork.CommonDefaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static com.shazam.chimprunner.Configuration.Builder.configuration;
import static com.shazam.chimprunner.injector.GsonInjector.gson;
import static com.shazam.fork.utils.Utils.cleanFile;

public class ChimpCli {

    private static final Logger logger = LoggerFactory.getLogger(ChimpCli.class);

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
            ChimpConfiguration chimpUserConfiguration = gson().fromJson(configFileReader, ChimpConfiguration.class);

            Configuration configuration = configuration()
                    .withAndroidSdk(parsedArgs.sdk != null ? parsedArgs.sdk : cleanFile(CommonDefaults.ANDROID_SDK))
                    .withApplicationApk(parsedArgs.apk)
                    .withInstrumentationApk(parsedArgs.testApk)
                    .withOutput(chimpUserConfiguration.baseOutputDir != null ? cleanFile(chimpUserConfiguration.baseOutputDir) : cleanFile(Defaults.CHIMP_OUTPUT))
                    .withTestPackage(chimpUserConfiguration.testPackage)
                    .withTestClassRegex(chimpUserConfiguration.testClassRegex)
                    .withSerial(chimpUserConfiguration.serial)
                    .build();

            ChimpRunner chimpRunner = new ChimpRunner(configuration);
            if (!chimpRunner.run() && !chimpUserConfiguration.ignoreFailures) {
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find configuration file", e);
            System.exit(1);
        }
    }
}
