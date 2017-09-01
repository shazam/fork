package com.shazam.forktest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassWithNoIgnoredMethodsTest {
    @Test
    public void firstTestMethod() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void secondTestMethod() {
        assertEquals(4, 2 + 2);
    }

}