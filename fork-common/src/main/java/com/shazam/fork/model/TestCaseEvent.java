package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {
    @Nonnull
    private final String testMethod;
    @Nonnull
    private final String testClass;

    private final boolean isIgnored;
    @Nonnull
    private final List<String> permissionsToRevoke;
    @Nonnull
    private final Map<String, String> properties;

    private final boolean isParameterized;

    private TestCaseEvent(Builder builder) {
        this.testClass = builder.testClass;
        this.testMethod = builder.testMethod;
        this.isIgnored = builder.isIgnored;
        this.permissionsToRevoke = builder.permissionsToRevoke;
        this.properties = builder.properties;
        this.isParameterized = builder.isParameterized;
    }

    @Nonnull
    public TestIdentifier toTestIdentifier() {
        return new TestIdentifier(testClass, testMethod);
    }

    @Nonnull
    public String getTestFullName() {
        return testClass + "#" + testMethod;
    }

    @Nonnull
    public String getTestMethod() {
        return testMethod;
    }

    @Nonnull
    public String getTestClass() {
        return testClass;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    @Nonnull
    public List<String> getPermissionsToRevoke() {
        return unmodifiableList(permissionsToRevoke);
    }

    @Nonnull
    public Map<String, String> getProperties() {
        return unmodifiableMap(properties);
    }

    public boolean isParameterized() {
        return isParameterized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCaseEvent that = (TestCaseEvent) o;

        if (isIgnored != that.isIgnored) return false;
        if (!testMethod.equals(that.testMethod)) return false;
        if (!testClass.equals(that.testClass)) return false;
        if (!permissionsToRevoke.equals(that.permissionsToRevoke)) return false;
        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        int result = testMethod.hashCode();
        result = 31 * result + testClass.hashCode();
        result = 31 * result + (isIgnored ? 1 : 0);
        result = 31 * result + permissionsToRevoke.hashCode();
        result = 31 * result + properties.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return reflectionToString(this, SIMPLE_STYLE);
    }

    public static class Builder {
        private String testClass;
        private String testMethod;
        private boolean isIgnored;
        private List<String> permissionsToRevoke = new ArrayList<>();
        private Map<String, String> properties = new HashMap<>();
        private boolean isParameterized = false;

        @Nonnull
        public static Builder testCaseEvent(@Nonnull TestIdentifier testIdentifier) {
            return testCaseEvent()
                    .withTestClass(testIdentifier.getClassName())
                    .withTestMethod(testIdentifier.getTestName());
        }

        @Nonnull
        public static Builder testCaseEvent() {
            return new Builder();
        }

        @Nonnull
        public Builder withTestClass(@Nonnull String testClass) {
            this.testClass = testClass;
            return this;
        }

        @Nonnull
        public Builder withTestMethod(@Nonnull String testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        @Nonnull
        public Builder withIsIgnored(boolean isIgnored) {
            this.isIgnored = isIgnored;
            return this;
        }

        @Nonnull
        public Builder withPermissionsToRevoke(@Nonnull List<String> permissionsToRevoke) {
            this.permissionsToRevoke.clear();
            this.permissionsToRevoke.addAll(permissionsToRevoke);
            return this;
        }

        @Nonnull
        public Builder withProperties(@Nonnull Map<String, String> properties) {
            this.properties.clear();
            this.properties.putAll(properties);
            return this;
        }

        @Nonnull
        public Builder withIsParameterized(boolean flag) {
            this.isParameterized = flag;
            return this;
        }

        @Nonnull
        public TestCaseEvent build() {
            return new TestCaseEvent(this);
        }
    }
}
