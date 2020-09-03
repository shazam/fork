package com.shazam.forktest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.shazam.forktest.ExcludedAnnotation;

public class ClassWithExcludedAnnotationTest {
    @Test
    public void includedMethod() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @ExcludedAnnotation
    public void excludedMethod() {
        assertEquals(4, 2 + 2);
    }

}
