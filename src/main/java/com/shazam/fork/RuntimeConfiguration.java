package com.shazam.fork;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.pooling.ComputedPoolsSelector;
import com.shazam.fork.pooling.SerialBasedPools;

import java.util.Collection;

/**
 * Holds information about the runtime setup: pools and test suite information.
 */
public class RuntimeConfiguration {

    private final String filterPattern;
    private final boolean useTabletFlag;
    private final SerialBasedPools serialBasedPools;
    private final ComputedPoolsSelector computedPoolsSelector;
    private final boolean createPoolForEachDevice;
    private final Collection<String> excludedSerials;
    private final String title;
    private final String subtitle;
    private final IRemoteAndroidTestRunner.TestSize testSize;

    private RuntimeConfiguration(Builder builder) {
        this.filterPattern = builder.filterPattern;
        this.useTabletFlag = builder.useTabletFlag;
        this.serialBasedPools = builder.serialBasedPools;
        this.computedPoolsSelector = builder.computedPoolsSelector;
        this.excludedSerials = builder.excludedSerials;
        this.createPoolForEachDevice = builder.createPoolForEachDevice;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.testSize = builder.testSize;
    }

    public static class Builder {
        private String filterPattern;
        private boolean useTabletFlag;
        private SerialBasedPools serialBasedPools;
        private ComputedPoolsSelector computedPoolsSelector;
        private Collection<String> excludedSerials;
        private boolean createPoolForEachDevice;
        private String title;
        private String subtitle;
        private IRemoteAndroidTestRunner.TestSize testSize;

        public static Builder aRuntimeConfiguration() {
            return new Builder();
        }

        public Builder withFilterPattern(String filterPattern) {
            this.filterPattern = filterPattern;
            return this;
        }

        public Builder whichUsesTabletFlag(boolean useTabletFlag) {
            this.useTabletFlag = useTabletFlag;
            return this;
        }

        public Builder withSerialBasedPools(SerialBasedPools serialBasedPools) {
            this.serialBasedPools = serialBasedPools;
            return this;
        }

        public Builder withComputedPoolsSelector(ComputedPoolsSelector computedPoolsSelector) {
            this.computedPoolsSelector = computedPoolsSelector;
            return this;
        }

        public Builder withExcludedSerials(Collection<String> excludedSerials) {
            this.excludedSerials = excludedSerials;
            return this;
        }

        public Builder whichCreatesPoolForEachDevice(boolean createPoolForEachDevice) {
            this.createPoolForEachDevice = createPoolForEachDevice;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder withTestSize(IRemoteAndroidTestRunner.TestSize testSize) {
            this.testSize = testSize;
            return this;
        }

        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(this);
        }
    }

    public String getFilterPattern() {
        return filterPattern;
    }

    public boolean isUsingTabletFlag() {
        return useTabletFlag;
    }

    public SerialBasedPools getSerialBasedPools() {
        return serialBasedPools;
    }

    public ComputedPoolsSelector getComputedPoolsSelector() {
        return computedPoolsSelector;
    }

    public boolean isCreatingPoolForEachDevice() {
        return createPoolForEachDevice;
    }

    public Collection<String> getExcludedSerials() {
        return excludedSerials;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public IRemoteAndroidTestRunner.TestSize getTestSize() {
        return testSize;
    }
}
