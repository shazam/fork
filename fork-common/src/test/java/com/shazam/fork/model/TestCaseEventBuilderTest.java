package com.shazam.fork.model;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TestCaseEventBuilderTest {

    @Test
    public void twoObjectsWithEqualParametersShouldAlwaysBeEqual() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("key", "value");

        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withIsIgnored(true)
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .withProperties(properties)
                .build();
        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withIsIgnored(true)
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .withProperties(properties)
                .build();

        assertThat(testCaseEventA, equalTo(testCaseEventB));
    }

    @Test
    public void twoObjectsWithDifferentParametersShouldNotBeEqual() {
        HashMap<String, String> propertiesA = new HashMap<>();
        propertiesA.put("key", "value");

        HashMap<String, String> propertiesB = new HashMap<>();
        propertiesB.put("key2", "value2");

        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withIsIgnored(true)
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .withProperties(propertiesA)
                .build();
        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withIsIgnored(true)
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .withProperties(propertiesB)
                .build();

        assertThat(testCaseEventA, not(equalTo(testCaseEventB)));
    }
}