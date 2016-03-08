package com.shazam.fork.runner;

import com.shazam.fork.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryWatchdog {

    private AtomicInteger totalAllowedRetryQuota;

    public RetryWatchdog(Configuration configuration) {
        totalAllowedRetryQuota = new AtomicInteger(configuration.getTotalAllowedRetryQuota());
    }

    public boolean allowRetry() {
        return  totalAllowedRetryQuota.get() > 0 && totalAllowedRetryQuota.getAndDecrement() >= 0;
    }
}
