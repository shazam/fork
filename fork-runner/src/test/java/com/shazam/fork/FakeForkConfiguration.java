package com.shazam.fork;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.system.axmlparser.ApplicationInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

public final class FakeForkConfiguration implements ForkConfiguration {
    private FakeForkConfiguration() {
    }

    @Nonnull
    public static FakeForkConfiguration aFakeForkConfiguration() {
        return new FakeForkConfiguration();
    }

    @Nonnull
    @Override
    public File getAndroidSdk() {
        return new File("");
    }

    @Nonnull
    @Override
    public File getApplicationApk() {
        return new File("");
    }

    @Nonnull
    @Override
    public File getInstrumentationApk() {
        return new File("");
    }

    @Nonnull
    @Override
    public String getApplicationPackage() {
        return "";
    }

    @Nonnull
    @Override
    public String getInstrumentationPackage() {
        return "";
    }

    @Nonnull
    @Override
    public String getTestRunnerClass() {
        return "";
    }

    @Nonnull
    @Override
    public File getOutput() {
        return new File("");
    }

    @Nonnull
    @Override
    public String getTitle() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubtitle() {
        return "";
    }

    @Nonnull
    @Override
    public Pattern getTestClassPattern() {
        return Pattern.compile(".*");
    }

    @Nonnull
    @Override
    public String getTestPackage() {
        return "";
    }

    @Override
    public long getTestOutputTimeout() {
        return 0;
    }

    @Nullable
    @Override
    public IRemoteAndroidTestRunner.TestSize getTestSize() {
        return null;
    }

    @Nonnull
    @Override
    public Collection<String> getExcludedSerials() {
        return emptyList();
    }

    @Override
    public boolean canFallbackToScreenshots() {
        return false;
    }

    @Override
    public int getTotalAllowedRetryQuota() {
        return 0;
    }

    @Override
    public int getRetryPerTestCaseQuota() {
        return 0;
    }

    @Override
    public boolean isCoverageEnabled() {
        return false;
    }

    @Override
    public PoolingStrategy getPoolingStrategy() {
        return null;
    }

    @Override
    public boolean isAutoGrantingPermissions() {
        return false;
    }

    @Override
    public String getExcludedAnnotation() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }
}
