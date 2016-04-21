package com.shazam.fork;

import com.shazam.fork.pooling.ComputedPoolsConfiguration;
import com.shazam.fork.pooling.SerialBasedPools;

import java.util.Collection;

/**
 * Holds information about the runtime setup: pools and test suite information.
 */
public class RuntimeConfiguration {

    private final boolean useTabletFlag;
    private final SerialBasedPools serialBasedPools;
    private final ComputedPoolsConfiguration computedPoolsConfiguration;
    private final boolean createPoolForEachDevice;
    private final Collection<String> excludedSerials;

    private RuntimeConfiguration(Builder builder) {
        this.useTabletFlag = builder.useTabletFlag;
        this.serialBasedPools = builder.serialBasedPools;
        this.computedPoolsConfiguration = builder.computedPoolsConfiguration;
        this.excludedSerials = builder.excludedSerials;
        this.createPoolForEachDevice = builder.createPoolForEachDevice;
    }

    public boolean isUsingTabletFlag() {
        return useTabletFlag;
    }

    public SerialBasedPools getSerialBasedPools() {
        return serialBasedPools;
    }

    public ComputedPoolsConfiguration getComputedPoolsConfiguration() {
        return computedPoolsConfiguration;
    }

    public boolean isCreatingPoolForEachDevice() {
        return createPoolForEachDevice;
    }

    public Collection<String> getExcludedSerials() {
        return excludedSerials;
    }

    public static class Builder {
        private boolean useTabletFlag;
        private SerialBasedPools serialBasedPools;
        private ComputedPoolsConfiguration computedPoolsConfiguration;
        private Collection<String> excludedSerials;
        private boolean createPoolForEachDevice;

        public static Builder aRuntimeConfiguration() {
            return new Builder();
        }

        public Builder whichUsesTabletFlag(boolean useTabletFlag) {
            this.useTabletFlag = useTabletFlag;
            return this;
        }

        public Builder withSerialBasedPools(SerialBasedPools serialBasedPools) {
            this.serialBasedPools = serialBasedPools;
            return this;
        }

        public Builder withComputedPoolsConfiguration(ComputedPoolsConfiguration computedPoolsSelector) {
            this.computedPoolsConfiguration = computedPoolsSelector;
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

        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(this);
        }
    }
}
