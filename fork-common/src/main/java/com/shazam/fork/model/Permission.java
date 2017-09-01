/*
 * Copyright 2017 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.model;

public class Permission {

    private  final String permissionName;

    private int minSdkVersion = 1;
    private int maxSdkVersion = Integer.MAX_VALUE;


    private Permission(Builder builder) {
        this.permissionName = builder.permissionName;
        this.minSdkVersion = builder.minSdkVersion;
        this.maxSdkVersion = builder.maxSdkVersion;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public int getMaxSdkVersion() {
        return maxSdkVersion;
    }


    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (minSdkVersion != that.minSdkVersion) return false;
        if (maxSdkVersion != that.maxSdkVersion) return false;
        return permissionName != null ? permissionName.equals(that.permissionName) : that.permissionName == null;
    }

    @Override
    public int hashCode() {
        int result = permissionName != null ? permissionName.hashCode() : 0;
        result = 31 * result + minSdkVersion;
        result = 31 * result + maxSdkVersion;
        return result;
    }

    public static class Builder {
        private String permissionName;
        private int minSdkVersion = 1;
        private int maxSdkVersion = Integer.MAX_VALUE;

        public static Builder permission() {
            return new Builder();
        }

        public Builder withPermissionName(String permission) {
            this.permissionName = permission;
            return this;
        }

        public Builder withMinSdkVersion(int minSdk) {
            this.minSdkVersion = minSdk;
            return this;
        }

        public Builder withMaxSdkVersion(int maxSdk) {
            this.maxSdkVersion = maxSdk;
            return this;
        }

        public Permission build() {
            return new Permission(this);
        }
    }
}
