package com.shazam.fork.injector;

import com.shazam.fork.io.FilenameCreator;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class FilenameCreatorInjector {

    public static FilenameCreator filenameCreator() {
        return new FilenameCreator(configuration().getOutput());
    }
}
