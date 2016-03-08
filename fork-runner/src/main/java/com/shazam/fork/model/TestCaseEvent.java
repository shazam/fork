package com.shazam.fork.model;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final boolean isIgnored;

    public TestCaseEvent(String testMethod, String testClass, boolean isIgnored) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public String getTestClass() {
        return testClass;
    }

    public boolean isIgnored() {
        return isIgnored;
    }
}
