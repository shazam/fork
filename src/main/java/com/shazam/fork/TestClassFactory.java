/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.shazam.fork.model.TestClass;
import com.shazam.fork.model.TestMethod;

import org.jf.dexlib.*;

import java.util.Collection;

import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.model.TestClass.Builder.testClass;
import static com.shazam.fork.model.TestMethod.Builder.testMethod;
import static java.util.Collections.emptyList;

public class TestClassFactory {
    private static final String TEST_TYPE = "Lorg/junit/Test;";
    private static final String TEST_TYPE_IGNORE = "Lorg/junit/Ignore;";

    TestClass createTestFromDexClass(ClassDefItem classDefItem) {
        String name = getName(classDefItem);
        Collection<TestMethod> methods = getMethods(classDefItem);

        return testClass()
                .withName(name)
                .withMethods(methods)
                .build();
    }

    private String getName(ClassDefItem classDefItem) {
        String typeDescriptor = classDefItem.getClassType().getTypeDescriptor();
        return typeDescriptor.substring(1, typeDescriptor.length() - 1).replace('/', '.');
    }

    private Collection<TestMethod> getMethods(ClassDefItem classDefItem) {
        AnnotationDirectoryItem annotations = classDefItem.getAnnotations();
        if (annotations == null) {
            return emptyList();
        }
        return extractTestMethods(annotations);
    }

    private Collection<TestMethod> extractTestMethods(AnnotationDirectoryItem annotations) {
        Collection<AnnotationDirectoryItem.MethodAnnotation> testMethods = filter(annotations.getMethodAnnotations(), testMethodsPredicate());
        return transform(testMethods, convertToForkTestMethod(isClassIgnored(annotations)));
    }

    private Predicate<? super AnnotationDirectoryItem.MethodAnnotation> testMethodsPredicate() {
        return new Predicate<AnnotationDirectoryItem.MethodAnnotation>() {
            @Override
            public boolean apply(@Nullable AnnotationDirectoryItem.MethodAnnotation input) {
                if (input == null) {
                    return false;
                }
                AnnotationItem[] annotations = input.annotationSet.getAnnotations();
                for (AnnotationItem annotation : annotations) {
                    if (TEST_TYPE.equals(stringType(annotation))) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    private Function<? super AnnotationDirectoryItem.MethodAnnotation, TestMethod> convertToForkTestMethod(
            final boolean classIsIgnored) {
        return new Function<AnnotationDirectoryItem.MethodAnnotation, TestMethod>() {
            @Nullable
            @Override
            public TestMethod apply(AnnotationDirectoryItem.MethodAnnotation input) {
                String name = input.method.getMethodName().getStringValue();
                TestMethod.Builder testMethodBuilder = testMethod().withName(name);
                if (findIfMethodIsIgnored(input)) {
                    testMethodBuilder.willBeIgnored();
                }

                return testMethodBuilder.build();
            }

            private boolean findIfMethodIsIgnored(AnnotationDirectoryItem.MethodAnnotation input) {
                if (classIsIgnored) {
                    return true;
                }

                AnnotationItem[] annotationItems = input.annotationSet.getAnnotations();
                for (AnnotationItem annotation : annotationItems) {
                    if (TEST_TYPE_IGNORE.equals(stringType(annotation))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private boolean isClassIgnored(AnnotationDirectoryItem annotationDirectoryItem) {
        AnnotationSetItem classAnnotations = annotationDirectoryItem.getClassAnnotations();
        if (classAnnotations == null) {
            return false;
        }
        AnnotationItem[] annotations = classAnnotations.getAnnotations();
        for (AnnotationItem annotation : annotations) {
            if (TEST_TYPE_IGNORE.equals(stringType(annotation))) {
                return true;
            }
        }
        return false;
    }

    private String stringType(AnnotationItem annotation) {
        return annotation.getEncodedAnnotation().annotationType.getTypeDescriptor();
    }
}
