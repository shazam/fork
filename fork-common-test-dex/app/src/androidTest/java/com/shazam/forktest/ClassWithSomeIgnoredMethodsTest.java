package com.shazam.forktest;

import org.junit.Ignore;
import org.junit.Test;

public class ClassWithSomeIgnoredMethodsTest {
    @Test
    public void nonIgnoredTestMethod() {
    }

    @Test
    @Ignore
    public void ignoredTestMethod() {
    }
}
