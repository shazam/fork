package com.shazam.fork.model;

import org.junit.Test;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestCaseEventBuilderTest {

    @Test
    public void testCaseEventsAreEqualWhenTestClassAndTestMethodAreTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(false)
                .withProperties(otherProperties())
                .build();

        assertEquals(testCaseEventA, testCaseEventB);
    }

    @Test
    public void testCaseEventsAreEqualWhenTestClassAndTestMethodAndIgnoreAndPropertiesAreTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        assertEquals(testCaseEventA, testCaseEventB);
    }

    @Test
    public void testCaseEventsAreNotEqualWhenTestClassIsNotTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("anothertestClass")
                .withTestMethod("testMethod")
                .build();

        assertNotEquals(testCaseEventA, testCaseEventB);
    }

    @Test
    public void testCaseEventsAreNotEqualWhenTestMethodIsNotTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("anotherTestMethod")
                .build();

        assertNotEquals(testCaseEventA, testCaseEventB);
    }

    @Test
    public void testCaseEventsHaveSameHashCodeWhenTestClassAndTestMethodAreTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(false)
                .withProperties(otherProperties())
                .build();

        assertEquals(testCaseEventA.hashCode(), testCaseEventB.hashCode());
    }

    @Test
    public void testCaseEventsHaveSameHashCodeWhenTestClassAndTestMethodAndIgnoreAndPropertiesAreTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .withIsIgnored(true)
                .withProperties(properties())
                .build();

        assertEquals(testCaseEventA.hashCode(), testCaseEventB.hashCode());
    }

    @Test
    public void testCaseEventsHaveDifferentHashCodeWhenTestClassIsNotTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("anothertestClass")
                .withTestMethod("TestMethod")
                .build();

        assertNotEquals(testCaseEventA.hashCode(), testCaseEventB.hashCode());
    }

    @Test
    public void testCaseEventsHaveDifferentHashCodeWhenTestMethodIsNotTheSame() {
        TestCaseEvent testCaseEventA = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("testMethod")
                .build();

        TestCaseEvent testCaseEventB = new TestCaseEvent.Builder()
                .withTestClass("testClass")
                .withTestMethod("anotherTestMethod")
                .build();

        assertNotEquals(testCaseEventA.hashCode(), testCaseEventB.hashCode());
    }

    private HashMap<String, String> properties() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("key", "value");
        return properties;
    }

    private HashMap<String, String> otherProperties() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("anotherKey", "anotherValue");
        return properties;
    }
}
