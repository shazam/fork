package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;

import java.util.List;

import javax.annotation.Nonnull;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final boolean isIgnored;
    private final List<String> permissionsToRevoke;

    private TestCaseEvent(String testMethod, String testClass, boolean isIgnored, List<String> permissionsToRevoke) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.permissionsToRevoke = permissionsToRevoke;
    }

    public static TestCaseEvent newTestCase(String testMethod, String testClass, boolean isIgnored, List<String> permissionsToRevoke) {
        return new TestCaseEvent(testMethod, testClass, isIgnored, permissionsToRevoke);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier ) {
        return new TestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), false, emptyList());
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

    public List<String> getPermissionsToRevoke() {
        return permissionsToRevoke;
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
