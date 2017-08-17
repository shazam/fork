package com.shazam.fork.system.axmlparser;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;

public class ApplicationInfo {

    private final List<String> permissions;

    public ApplicationInfo(@Nonnull List<String> permissions) {
        this.permissions = ImmutableList.copyOf(permissions);
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
