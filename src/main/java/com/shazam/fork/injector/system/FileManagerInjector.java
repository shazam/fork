package com.shazam.fork.injector.system;

import com.shazam.fork.system.io.FileManager;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class FileManagerInjector {

    public static FileManager fileManager() {
        return new FileManager(configuration().getOutput());
    }
}
