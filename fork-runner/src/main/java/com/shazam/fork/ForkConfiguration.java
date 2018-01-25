package com.shazam.fork;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.system.axmlparser.ApplicationInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.regex.Pattern;

public interface ForkConfiguration {
    @Nonnull
    File getAndroidSdk();

    @Nonnull
    File getApplicationApk();

    @Nonnull
    File getInstrumentationApk();

    @Nonnull
    String getApplicationPackage();

    @Nonnull
    String getInstrumentationPackage();

    @Nonnull
    String getTestRunnerClass();

    @Nonnull
    File getOutput();

    @Nonnull
    String getTitle();

    @Nonnull
    String getSubtitle();

    @Nonnull
    Pattern getTestClassPattern();

    @Nonnull
    String getTestPackage();

    long getTestOutputTimeout();

    @Nullable
    IRemoteAndroidTestRunner.TestSize getTestSize();

    @Nonnull
    Collection<String> getExcludedSerials();

    boolean canFallbackToScreenshots();

    int getTotalAllowedRetryQuota();

    int getRetryPerTestCaseQuota();

    boolean isCoverageEnabled();

    PoolingStrategy getPoolingStrategy();

    boolean isAutoGrantingPermissions();

    String getExcludedAnnotation();

    ApplicationInfo getApplicationInfo();
}
