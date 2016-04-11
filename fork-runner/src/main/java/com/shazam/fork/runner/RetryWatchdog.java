package com.shazam.fork.runner;

import com.shazam.fork.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryWatchdog {
    private static final Logger logger = LoggerFactory.getLogger(RetryWatchdog.class);

    private final int maxRetryPerTestCaseQuota;
    private AtomicInteger totalAllowedRetryLeft;
    private StringBuilder logBuilder = new StringBuilder();

    public RetryWatchdog(Configuration configuration) {
        totalAllowedRetryLeft = new AtomicInteger(configuration.getTotalAllowedRetryQuota());
        maxRetryPerTestCaseQuota = configuration.getRetryPerTestCaseQuota();
    }

    public boolean requestRetry(int currentSingleTestCaseFailures) {
        boolean totalAllowedRetryAvailable = totalAllowedRetryAvailable();
        boolean singleTestAllowed = currentSingleTestCaseFailures <= maxRetryPerTestCaseQuota;
        boolean result = totalAllowedRetryAvailable && singleTestAllowed;

        log(currentSingleTestCaseFailures, singleTestAllowed, result);
        return result;
    }

    private boolean totalAllowedRetryAvailable() {
        return totalAllowedRetryLeft.get() > 0 && totalAllowedRetryLeft.getAndDecrement() >= 0;
    }

    private void log(int testCaseFailures, boolean singleTestAllowed, boolean result) {
        if(logger.isDebugEnabled()) {
            logBuilder.setLength(0); //clean up.
            logBuilder.append("Retry requested ")
                    .append(result ? " and allowed. " : " but not allowed. ")
                    .append("Total retry left :").append(totalAllowedRetryLeft.get())
                    .append(" and Single Test case retry left: ")
                    .append(singleTestAllowed ? maxRetryPerTestCaseQuota - testCaseFailures : 0);
            logger.debug(logBuilder.toString());
        }
    }
}
