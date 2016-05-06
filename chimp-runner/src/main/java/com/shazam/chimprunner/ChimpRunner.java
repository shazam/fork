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

import com.android.ddmlib.DdmPreferences;
import com.shazam.fork.device.DeviceCouldNotBeFoundException;
import com.shazam.fork.device.DeviceLoader;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.suite.NoTestCasesFoundException;
import com.shazam.fork.suite.TestSuiteLoader;
import com.shazam.fork.system.adb.Adb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.shazam.chimprunner.injector.ConfigurationInjector.configuration;
import static com.shazam.chimprunner.injector.ConfigurationInjector.setConfiguration;
import static com.shazam.chimprunner.injector.ResultsStorageInjector.resultsStorage;
import static com.shazam.chimprunner.injector.device.DeviceLoaderInjector.deviceLoader;
import static com.shazam.chimprunner.injector.suite.TestSuiteLoaderInjector.testSuiteLoader;
import static com.shazam.chimprunner.injector.system.AdbInjector.adb;
import static com.shazam.chimprunner.injector.system.InstallerInjector.installer;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatPeriod;

public class ChimpRunner {
    private static final Logger logger = LoggerFactory.getLogger(ChimpRunner.class);

    private final File output;
    private final DeviceLoader deviceLoader;
    private final String serial;
    private final TestSuiteLoader testSuiteLoader;
    private final Adb adb;
    private final ResultsStorage resultsStorage;

    public ChimpRunner(Configuration configuration) {
        this.output = configuration.getOutput();
        setConfiguration(configuration);
        adb = adb();
        deviceLoader = deviceLoader();
        serial = configuration.getSerial();
        testSuiteLoader = testSuiteLoader();
        resultsStorage = resultsStorage();
    }

    public boolean run() {
        long startOfTestsMs = nanoTime();
        try {
            prepareOutputDirectory();
            DdmPreferences.setTimeOut(Defaults.DDMS_TIMEOUT);
            Device device = deviceLoader.loadDevice(serial);
            Collection<TestCaseEvent> testCaseEvents = testSuiteLoader.loadTestSuite();
            PerformanceTestRunner performanceTestRunner = new PerformanceTestRunner(
                    installer(),
                    configuration().getInstrumentationPackage(),
                    configuration().getTestRunnerClass());
            Map<TestCaseEvent, Double> results = performanceTestRunner.run(device, testCaseEvents);
            resultsStorage.storeResults(results);
            return true;
        } catch (IOException e) {
            logger.error("Error while running ChimpRunner", e);
            return false;
        } catch (DeviceCouldNotBeFoundException e) {
            logger.error("Error while finding test devices", e);
            return false;
        } catch (NoTestCasesFoundException e) {
            logger.error("Error when scanning for test cases", e);
            return false;
        } catch (TestFailureException e) {
            logger.error("Stopped execution because of test failures.", e);
            return false;
        } catch (ResultStorageException e) {
            logger.error("Error while storing results.", e);
            return false;
        } finally {
            long duration = millisSinceNanoTime(startOfTestsMs);
            logger.info(formatPeriod(0, duration, "'Total time taken:' H 'hours' m 'minutes' s 'seconds'"));
            adb.terminate();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void prepareOutputDirectory() throws IOException {
        deleteDirectory(output);
        output.mkdirs();
    }

}
