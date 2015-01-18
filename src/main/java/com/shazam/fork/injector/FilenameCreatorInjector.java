package com.shazam.fork.injector;

import com.shazam.fork.io.FileManager;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class FilenameCreatorInjector {

    public static FileManager filenameCreator() {
        return new FileManager(configuration().getOutput());
    }
}
