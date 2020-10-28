package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
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

    @Test
    public void generatedTestIdentifierHasCorrectClassName() {
        TestCaseEvent testCaseEvent = new TestCaseEvent.Builder()
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .build();
        TestIdentifier testIdentifier = testCaseEvent.toTestIdentifier();

        assertEquals(testCaseEvent.getTestClass(),testIdentifier.getClassName());
    }

    @Test
    public void generatedTestIdentifierHasCorrectTestName() {
        TestCaseEvent testCaseEvent = new TestCaseEvent.Builder()
                .withTestClass("TestClass")
                .withTestMethod("testMethod")
                .build();
        TestIdentifier testIdentifier = testCaseEvent.toTestIdentifier();

        assertEquals(testCaseEvent.getTestMethod(),testIdentifier.getTestName());
    }
}
