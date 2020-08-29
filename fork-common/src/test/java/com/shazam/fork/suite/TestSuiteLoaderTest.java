/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.suite;

import com.shazam.fork.io.DexFileExtractor;
import com.shazam.fork.model.TestCaseEvent;
import org.hamcrest.Matcher;
import org.jf.dexlib2.iface.DexFile;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shazam.fork.io.FakeDexFileExtractor.fakeDexFileExtractor;
import static com.shazam.fork.io.Files.convertFileToDexFile;
import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static com.shazam.fork.suite.FakeTestClassMatcher.fakeTestClassMatcher;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasItems;

/**
 * This test is based on the <code>tests.dex</code> file, which contains test classes with the following code:
 * <blockquote><pre>
 * {@literal}@Ignore
 * public class IgnoredClassTest {
 *    {@literal}@Test
 *    public void methodOfAnIgnoredTestClass() {
 *    }
 * }
 * <p>
 * public class ClassWithNoIgnoredMethodsTest {
 *    {@literal}@Test
 *    public void firstTestMethod() {
 *    }
 * <p>
 *    {@literal}@Test
 *    public void secondTestMethod() {
 *    }
 * }
 * <p>
 * public class ClassWithSomeIgnoredMethodsTest {
 *    {@literal}@Test
 *    public void nonIgnoredTestMethod() {
 *    }
 * <p>
 *    {@literal}@Test
 *    {@literal}@Ignore
 *    public void ignoredTestMethod() {
 *    }
 * }
 * </pre></blockquote>
 */
public class TestSuiteLoaderTest {
    private static final File ANY_INSTRUMENTATION_APK_FILE = null;

    private final DexFileExtractor fakeDexFileExtractor = fakeDexFileExtractor().thatReturns(testDexFile());
    private final TestClassMatcher fakeTestClassMatcher = fakeTestClassMatcher().thatAlwaysMatches();
    private TestSuiteLoader testSuiteLoader;

    private DexFile testDexFile() {
        URL testDexResourceUrl = this.getClass().getResource("/tests.dex");
        String testDexFile = testDexResourceUrl.getFile();
        File file = new File(testDexFile);
        return convertFileToDexFile().apply(file);
    }

    @Before
    public void setUp() {
        testSuiteLoader = new TestSuiteLoader(ANY_INSTRUMENTATION_APK_FILE, fakeDexFileExtractor, fakeTestClassMatcher);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setsIgnoredFlag() throws Exception {
        assertThat(testSuiteLoader.loadTestSuite(), hasItems(
                sameTestEventAs("methodOfAnIgnoredTestClass", "com.shazam.forktest.IgnoredClassTest", true),
                sameTestEventAs("firstTestMethod", "com.shazam.forktest.ClassWithNoIgnoredMethodsTest", false),
                sameTestEventAs("secondTestMethod", "com.shazam.forktest.ClassWithNoIgnoredMethodsTest", false),
                sameTestEventAs("nonIgnoredTestMethod", "com.shazam.forktest.ClassWithSomeIgnoredMethodsTest", false),
                sameTestEventAs("ignoredTestMethod", "com.shazam.forktest.ClassWithSomeIgnoredMethodsTest", true)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void populatesRevokedPermissions() throws Exception {
        String testClass = "com.shazam.forktest.RevokePermissionsClassTest";
        List<String> permissions = asList("android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION");

        assertThat(testSuiteLoader.loadTestSuite(), hasItems(
                sameTestEventAs("methodAnnotatedWithRevokePermissionsTest", testClass, false, permissions),
                sameTestEventAs("methodAnnotatedWithEmptyRevokePermissionsTest", testClass, false)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void populatesTestProperties() throws Exception {
        String testClass = "com.shazam.forktest.PropertiesClassTest";
        Map<String, String> singlePropertyMap = singletonMap("foo", "bar");
        Map<String, String> multiPropertiesMap = new HashMap();
        multiPropertiesMap.put("foo", "bar");
        multiPropertiesMap.put("bux", "poi");

        assertThat(testSuiteLoader.loadTestSuite(), hasItems(
                sameTestEventAs("methodWithProperties", testClass, singlePropertyMap),
                sameTestEventAs("methodWithMultipleProperties", testClass, multiPropertiesMap),
                sameTestEventAs("methodWithEmptyProperties", testClass, emptyMap()),
                sameTestEventAs("methodWithUnmatchedKey", testClass, singlePropertyMap)));
    }

    @Nonnull
    private Matcher<TestCaseEvent> sameTestEventAs(String testMethod, String testClass, Map<String, String> properties) {
        return sameBeanAs(
                testCaseEvent()
                        .withTestClass(testClass)
                        .withTestMethod(testMethod)
                        .withProperties(properties)
                        .build()
        );
    }

    @Nonnull
    private Matcher<TestCaseEvent> sameTestEventAs(String testMethod, String testClass, boolean isIgnored) {
        return sameBeanAs(
                testCaseEvent()
                        .withTestClass(testClass)
                        .withTestMethod(testMethod)
                        .withIsIgnored(isIgnored)
                        .build()
        );
    }

    @Nonnull
    private Matcher<TestCaseEvent> sameTestEventAs(String testMethod, String testClass, boolean isIgnored, List<String> permissions) {
        return sameBeanAs(
                testCaseEvent()
                        .withTestClass(testClass)
                        .withTestMethod(testMethod)
                        .withIsIgnored(isIgnored)
                        .withPermissionsToRevoke(permissions)
                        .build()
        );
    }
}
