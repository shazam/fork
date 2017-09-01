package com.shazam.forktest;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassWithSomeIgnoredMethodsTest {
    @Test
    public void nonIgnoredTestMethod() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @Ignore
    public void ignoredTestMethod() {
        assertEquals(4, 2 + 2);
    }
}
