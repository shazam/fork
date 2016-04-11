package com.shazam.fork.runner;

import com.shazam.fork.Configuration;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RetryWatchdogTest {

    RetryWatchdog subject;

    @Test
    public void shouldAllowWhenTotalAndPerTestAreEquals() throws Exception {
        subject = new RetryWatchdog(aConfigWith(1, 1));
        boolean actual = subject.requestRetry(1);

        assertTrue(actual);
    }

    @Test
    public void shouldNotAllowWhenTotalAndPerTestAreLessThenActualFailures() throws Exception {
        subject = new RetryWatchdog(aConfigWith(1, 1));
        boolean actual = subject.requestRetry(2);

        assertFalse(actual);
    }

    @Test
    public void shouldNotAllowWhenTooManyTotalFailuresAlreadyRequested() throws Exception {
        subject = new RetryWatchdog(aConfigWith(1, Integer.MAX_VALUE));
        assertTrue(subject.requestRetry(0));
        assertFalse(subject.requestRetry(0));
    }

    @Test
    public void shouldNotAllowWhenNoRetryAllowed() throws Exception {
        subject = new RetryWatchdog(aConfigWith(0, Integer.MAX_VALUE));
        assertFalse(subject.requestRetry(0));
    }

    private Configuration aConfigWith(int totalRetryAllow, int perTestTotalAllow) {
        //noinspection ConstantConditions
        return new Configuration(
                null, null, null, null, null, null, null, null, 0, false,
                totalRetryAllow,
                perTestTotalAllow);
    }
}