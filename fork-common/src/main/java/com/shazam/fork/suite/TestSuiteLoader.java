/*
 * Copyright 2016 Shazam Entertainment Limited
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

import org.jf.dexlib.*;

import java.io.File;
import java.util.*;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class TestSuiteLoader {
    private static final String TEST_ANNOTATION = "Lorg/junit/Test;";
    private static final String IGNORE_ANNOTATION = "Lorg/junit/Ignore;";

    private final File instrumentationApkFile;
    private final DexFileExtractor dexFileExtractor;
    private final TestClassMatcher testClassMatcher;

    public TestSuiteLoader(File instrumentationApkFile, DexFileExtractor dexFileExtractor, TestClassMatcher testClassMatcher) {
        this.instrumentationApkFile = instrumentationApkFile;
        this.dexFileExtractor = dexFileExtractor;
        this.testClassMatcher = testClassMatcher;
    }

    public Collection<TestCaseEvent> loadTestSuite() throws NoTestCasesFoundException {
        List<TestCaseEvent> testCaseEvents = dexFileExtractor.getDexFiles(instrumentationApkFile).stream()
                .map(dexFile -> dexFile.ClassDefsSection.getItems())
                .flatMap(Collection::stream)
                .filter(c -> testClassMatcher.matchesPatterns(c.getClassType().getTypeDescriptor()))
                .map(this::convertClassToTestCaseEvents)
                .flatMap(Collection::stream)
                .collect(toList());

        if (testCaseEvents.isEmpty()) {
            throw new NoTestCasesFoundException("No tests cases were found in the test APK: " + instrumentationApkFile.getAbsolutePath());
        }
        return testCaseEvents;
    }

    @Nonnull
    private List<TestCaseEvent> convertClassToTestCaseEvents(ClassDefItem classDefItem) {
        AnnotationDirectoryItem annotationDirectoryItem = classDefItem.getAnnotations();
        if (annotationDirectoryItem == null) {
            return emptyList();
        }

        List<TestCaseEvent> testCaseEvents = new ArrayList<>();
        for (AnnotationDirectoryItem.MethodAnnotation method : annotationDirectoryItem.getMethodAnnotations()) {
            asList(method.annotationSet.getAnnotations()).stream()
                    .filter(annotation -> TEST_ANNOTATION.equals(stringType(annotation)))
                    .map(annotation -> convertToTestCaseEvent(classDefItem, annotationDirectoryItem, method, annotation))
                    .forEach(testCaseEvents::add);
        }
        return testCaseEvents;
    }

    @Nonnull
    private TestCaseEvent convertToTestCaseEvent(ClassDefItem classDefItem,
                                                 AnnotationDirectoryItem annotationDirectoryItem,
                                                 AnnotationDirectoryItem.MethodAnnotation method,
                                                 AnnotationItem annotation) {
        String testMethod = method.method.getMethodName().getStringValue();
        String testClass = getClassName(classDefItem);
        boolean ignored = isClassIgnored(annotationDirectoryItem) || isMethodIgnored(annotation);
        return new TestCaseEvent(testMethod, testClass, ignored);
    }

    private String getClassName(ClassDefItem classDefItem) {
        String typeDescriptor = classDefItem.getClassType().getTypeDescriptor();
        return typeDescriptor.substring(1, typeDescriptor.length() - 1).replace('/', '.');
    }

    private boolean isMethodIgnored(AnnotationItem input) {
        return containsAnnotation(IGNORE_ANNOTATION, input);
    }

    private boolean isClassIgnored(AnnotationDirectoryItem annotationDirectoryItem) {
        AnnotationSetItem classAnnotations = annotationDirectoryItem.getClassAnnotations();
        if (classAnnotations == null) {
            return false;
        }
        return containsAnnotation(IGNORE_ANNOTATION, classAnnotations.getAnnotations());
    }

    private boolean containsAnnotation(String comparisonAnnotation, AnnotationItem... annotations) {
        return asList(annotations).stream()
                .filter(annotation -> comparisonAnnotation.equals(stringType(annotation)))
                .findFirst()
                .isPresent();
    }

    private String stringType(AnnotationItem annotation) {
        return annotation.getEncodedAnnotation().annotationType.getTypeDescriptor();
    }
}
