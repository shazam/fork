package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {
    private final String testMethod;
    private final String testClass;
    private final boolean isIgnored;
    private final List<String> permissionsToRevoke;
    private final Map<String, String> properties;

    private TestCaseEvent(Builder builder) {
        this.testClass = builder.testClass;
        this.testMethod = builder.testMethod;
        this.isIgnored = builder.isIgnored;
        this.permissionsToRevoke = builder.permissionsToRevoke;
        this.properties = builder.properties;
    }

    @Nonnull
    public static TestCaseEvent from(@Nonnull TestIdentifier testIdentifier) {
        return testCaseEvent()
                .withTestClass(testIdentifier.getClassName())
                .withTestMethod(testIdentifier.getTestName())
                .withIsIgnored(false)
                .build();
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

    public static class Builder {
        private String testClass;
        private String testMethod;
        private boolean isIgnored;
        private List<String> permissionsToRevoke = new ArrayList<>();
        private Map<String, String> properties = new HashMap<>();

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
        public TestCaseEvent build() {
            return new TestCaseEvent(this);
        }
    }
}
