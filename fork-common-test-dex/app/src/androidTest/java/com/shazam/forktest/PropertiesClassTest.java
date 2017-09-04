package com.shazam.forktest;

import com.shazam.fork.TestProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertiesClassTest {

    @Test
    @TestProperties(keys = {"foo"}, values = {"bar"})
    public void methodWithProperties() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @TestProperties(keys = {"foo", "bux"}, values = {"bar", "poi"})
    public void methodWithMultipleProperties() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @TestProperties()
    public void methodWithEmptyProperties() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @TestProperties(keys = {"foo", "unmatchedkey"}, values = {"bar"})
    public void methodWithUnmatchedKey() {
        assertEquals(4, 2 + 2);
    }
}

