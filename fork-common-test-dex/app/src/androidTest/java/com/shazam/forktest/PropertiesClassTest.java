package com.shazam.forktest;

import com.shazam.fork.TestProperty;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertiesClassTest {

    @Test
    @TestProperty(key = "foo", value = "bar")
    public void methodWithProperties() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @TestProperty(key = "foo", value = "bar")
    @TestProperty(key = "bux", value = "poi")
    public void methodWithMultipleProperties() {
        assertEquals(4, 2 + 2);
    }
}

