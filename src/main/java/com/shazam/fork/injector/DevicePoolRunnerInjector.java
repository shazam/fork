package com.shazam.fork.injector;

import com.shazam.fork.DevicePoolRunner;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;
import static com.shazam.fork.injector.FilenameCreatorInjector.filenameCreator;
import static com.shazam.fork.injector.GsonInjector.gson;
import static com.shazam.fork.injector.InstallerInjector.installer;
import static com.shazam.fork.injector.SwimlaneConsoleLoggerInjector.swimlaneConsoleLogger;

public class DevicePoolRunnerInjector {

    public static DevicePoolRunner devicePoolRunner() {
        return new DevicePoolRunner(configuration(), gson(), installer(), filenameCreator(), swimlaneConsoleLogger());
    }
}
