package com.shazam.forktest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.shazam.forktest.ExcludedAnnotation;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ParameterizedClassTest {
    private String id;
    private boolean flag;

    @Parameterized.Parameters(name = "{0}({1})")
    public static Collection params() {
        return Arrays.asList(new Object[][] {
                { "first", false },
                { "second", true },
                { "third", false },
        });
    }

    @Test
    public void test() {
        assertEquals(flag, ("second".equals(id)));
    }
}
