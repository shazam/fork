package com.shazam.fork.injector;

import com.shazam.fork.runner.RetryWatchdog;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class RetryWatchdogInjector {

    public static RetryWatchdog retryWatchdog(){
        return new RetryWatchdog(configuration());
    }
}
