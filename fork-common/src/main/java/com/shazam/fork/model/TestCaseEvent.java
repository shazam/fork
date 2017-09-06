package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final boolean isIgnored;
    private final List<String> permissionsToRevoke;
    private final Map<String, String> properties;

    private TestCaseEvent(String testMethod, String testClass, boolean isIgnored, List<String> permissionsToRevoke, Map<String, String> properties) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.permissionsToRevoke = permissionsToRevoke;
        this.properties = properties;
    }

    public static TestCaseEvent newTestCase(String testMethod, String testClass, boolean isIgnored, List<String> permissionsToRevoke, Map<String, String> properties) {
        return new TestCaseEvent(testMethod, testClass, isIgnored, permissionsToRevoke, properties);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier) {
        return new TestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), false, emptyList(), emptyMap());
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

    public Map<String, String> getProperties() {
        return properties;
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
