package com.shazam.fork.injector.system;

import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.ForkFileManager;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class FileManagerInjector {

    private FileManagerInjector() {
    }

    public static FileManager fileManager() {
        return new ForkFileManager(configuration().getOutput());
    }
}
