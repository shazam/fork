/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.suite;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestMethod;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.common.collect.FluentIterable.from;

public class TestClassLoader {
    private final TestClassScanner scanner;
    private final Predicate<TestMethod> VALID_METHOD = new Predicate<TestMethod>() {
        @Override
        public boolean apply(@Nullable TestMethod input) {
            return input != null;
        }
    };

    public TestClassLoader(TestClassScanner scanner) {
        this.scanner = scanner;
    }

    public List<TestCaseEvent> loadTestClasses() throws TestClassScanningException {
        List<TestClass> testClasses = scanner.scanForTestClasses();
        return from(testClasses).transformAndConcat(new Function<TestClass, Iterable<TestCaseEvent>>() {
            @Nullable
            @Override
            public Iterable<TestCaseEvent> apply(final @Nullable TestClass testClass) {
                return (testClass != null && testClass.getMethods() != null) ?
                        from(testClass.getMethods())
                        .filter(VALID_METHOD)
                        .transform(new Function<TestMethod, TestCaseEvent>() {
                            @Nullable
                            @Override
                            public TestCaseEvent apply(@Nullable TestMethod testMethod) {
                                assert testMethod != null;
                                return new TestCaseEvent(testMethod.getName(),
                                        testClass.getName(),
                                        testMethod.isIgnored());
                            }
                        }) : Collections.<TestCaseEvent>emptyList();

            }
        }).toList();
    }
}
