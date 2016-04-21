package com.shazam.fork.system;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.shazam.fork.pooling.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.pooling.SerialBasedPools.Builder.serialBasedPools;
import static com.shazam.fork.system.EnvironmentConstants.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperties;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.list;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class RuntimeConfigurationExtractor {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeConfigurationExtractor.class);

    @Nonnull
    public static Collection<String> extractExcludedSerials() {
        String excludedSerialsProperty = getProperties().getProperty(PARAMETER_EXCLUDED_SERIALS);
        Collection<String> excludedSerials = isBlank(excludedSerialsProperty) ?
                Collections.<String>emptyList(): asList(excludedSerialsProperty.split(","));
        documentRuntimeParameter(PARAMETER_EXCLUDED_SERIALS, excludedSerials,
                "Use -D{}=(Serial','?)* to exclude specific devices from running any tests");

        if (!excludedSerials.isEmpty()) {
            logger.info("Devices with serials {} are excluded from the tests", excludedSerials);
        }

        return excludedSerials;
    }

    public static boolean extractTabletFlag() {
        String tabletFlagParameter = valueFrom(PARAMETER_POOL_TABLET);
        documentRuntimeParameter(PARAMETER_POOL_TABLET, tabletFlagParameter,
                "Use -D{}=(true|false) to configure pools depending on their manufacturer's \'tablet\' flag " +
                        "(ro.build.characteristics)");
        return parseBoolean(tabletFlagParameter);
    }

    @Nonnull
    public static SerialBasedPools extractSerialBasedPools() {
        Properties properties = System.getProperties();
        Collection<String> keys = extractKeysWithSerials(properties);
        documentRuntimeParameter(PARAMETER_POOL_SERIAL_BASED, keys,
                "Use -D{}POOL_NAME=(Serial\',\'?)* to add devices with a given serial to a pool with given name," +
                        "e.g. hdpi=01234567,abcdefgh");

        SerialBasedPools.Builder serialBasedPoolsBuilder = serialBasedPools();
        for (String key : keys) {
            String poolName = key.replace(PARAMETER_POOL_SERIAL_BASED, "");
            String serialsParameter = (String) properties.get(key);
            List<String> serials = asList(serialsParameter.split(","));
            serialBasedPoolsBuilder.withSerialBasedPool(poolName, serials);
        }

        return serialBasedPoolsBuilder.build();
    }

    @Nullable
    public static ComputedPoolsConfiguration extractComputedPoolsConfiguration(
            Collection<ComputedPoolingStrategy> poolingStrategies,
            ComputedPoolsConfigurationFactory factory) {
        Properties properties = System.getProperties();
        Collection<String> keys = extractKeysWithComputedPools(properties);

        documentComputedPoolParameters(poolingStrategies, keys);
        return factory.createConfiguration(PARAMETER_POOL_COMPUTED, keys, properties, poolingStrategies);
    }

    /**
     * Unless explicitly switched off, we default this to true.
     * @return <code>true</code> by default or if switched on, <code>false</code> if switched off
     */
    public static boolean extractPoolPerDeviceFlag() {
        String poolPerDevice = valueFrom(PARAMETER_POOL_EACHDEVICE);
        documentRuntimeParameter(PARAMETER_POOL_EACHDEVICE, poolPerDevice,
                "Use -D{}=(true|false) to create a pool per device (a.k.a. Spoon mode). This is the default behaviour.");
        if (isBlank(poolPerDevice)) {
            return true;
        }
        return parseBoolean(poolPerDevice);
    }

    @Nullable
    public static IRemoteAndroidTestRunner.TestSize extractTestSize() {
        String testSizeParam = valueFrom(PARAMETER_TEST_SIZE);
        documentRuntimeParameter(PARAMETER_TEST_SIZE, testSizeParam,
                "Use -D{}=(small|medium|large) to run test methods with the corresponding size annotation");
        return testSizeParam != null ? IRemoteAndroidTestRunner.TestSize.getTestSize(testSizeParam) : null;
    }

    private static void documentComputedPoolParameters(Collection<ComputedPoolingStrategy> poolingStrategies,
                                                       Collection<String> keys) {
        documentRuntimeParameter(PARAMETER_POOL_COMPUTED, keys,
                "Use -D{}STRATEGY=(PoolName=LowerBound\',\'?)* to automatically create pools based on device characteristics");
        if (keys == null || keys.isEmpty()) {
            for (ComputedPoolingStrategy computedPoolingStrategy : poolingStrategies) {
                logger.warn("STRATEGY:={} - {}", computedPoolingStrategy.getBaseName(), computedPoolingStrategy.help());
            }
        }
    }

    private static Collection<String> extractKeysWithSerials(Properties properties) {
        return extractKeysWithPrefix(properties, PARAMETER_POOL_SERIAL_BASED);
    }

    private static Collection<String> extractKeysWithComputedPools(Properties properties) {
        return extractKeysWithPrefix(properties, PARAMETER_POOL_COMPUTED);
    }

    private static Collection<String> extractKeysWithPrefix(Properties properties, final String prefix) {
        Collection<String> keys = getKeys(properties);

        return filter(keys, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String key) {
                return key != null && key.startsWith(prefix);
            }
        });
    }

    private static String valueFrom(String propertyKey) {
        return getProperty(propertyKey);
    }

    private static void documentRuntimeParameter(String parameterName, String parameterValue, String message) {
        if (isBlank(parameterValue)) {
            logMessage(parameterName, message);
        }
    }

    private static void documentRuntimeParameter(String parameterName, Collection<String> parameterValues, String message) {
        if (parameterValues == null || parameterValues.isEmpty()) {
            logMessage(parameterName, message);
        }
    }

    private static void logMessage(String parameterName, String message) {
        logger.warn(message, parameterName);
    }

    private static Collection<String> getKeys(Properties properties) {
        return transform(list(properties.keys()), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return (String) input;
            }
        });
    }
}
