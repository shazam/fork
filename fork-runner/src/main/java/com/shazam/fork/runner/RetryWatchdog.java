package com.shazam.fork.runner;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryWatchdog {

    private AtomicInteger counter = new AtomicInteger(6);

    public boolean allowRetry() {
        return  counter.get() >= 0 && counter.getAndDecrement() > 0;
    }
}
