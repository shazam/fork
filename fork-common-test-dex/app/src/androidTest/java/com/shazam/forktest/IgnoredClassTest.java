package com.shazam.forktest;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore
public class IgnoredClassTest {

    @Test
    public void methodOfAnIgnoredTestClass() {
        assertEquals(4, 2 + 2);
    }
}

