package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final boolean isIgnored;

    private TestCaseEvent(String testMethod, String testClass, boolean isIgnored) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
    }

    public static TestCaseEvent newTestCase(String testMethod, String testClass, boolean isIgnored) {
        return new TestCaseEvent(testMethod, testClass, isIgnored);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier, boolean isIgnored) {
        return new TestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), isIgnored);
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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.testMethod, this.testClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseEvent other = (TestCaseEvent) obj;
        return Objects.equal(this.testMethod, other.testMethod)
                && Objects.equal(this.testClass, other.testClass);
    }

    @Override
    public String toString() {
        return reflectionToString(this, SIMPLE_STYLE);
    }
}
