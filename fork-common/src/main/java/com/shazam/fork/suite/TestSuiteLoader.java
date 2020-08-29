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

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.value.ArrayEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;

import javax.annotation.Nonnull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.shazam.fork.model.TestCaseEvent.Builder.testCaseEvent;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

public class TestSuiteLoader {
    private static final String TEST_ANNOTATION = "Lorg/junit/Test;";
    private static final String IGNORE_ANNOTATION = "Lorg/junit/Ignore;";
    private static final String REVOKE_PERMISSION_ANNOTATION = "Lcom/shazam/fork/RevokePermission;";
    private static final String TEST_PROPERTIES_ANNOTATION = "Lcom/shazam/fork/TestProperties;";

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
                .map(DexFile::getClasses)
                .flatMap(Collection::stream)
                .filter(c -> testClassMatcher.matchesPatterns(c.getType()))
                .map(this::convertClassToTestCaseEvents)
                .flatMap(Collection::stream)
                .collect(toList());

        if (testCaseEvents.isEmpty()) {
            throw new NoTestCasesFoundException("No tests cases were found in the test APK: " + instrumentationApkFile.getAbsolutePath());
        }
        return testCaseEvents;
    }

    @Nonnull
    private List<TestCaseEvent> convertClassToTestCaseEvents(ClassDef classDefItem) {
        List<TestCaseEvent> testCaseEvents = new ArrayList<>();

        Iterable<? extends Method> methods = classDefItem.getMethods();
        StreamSupport.stream(methods.spliterator(), false)
                .filter(method -> method.getAnnotations().stream().anyMatch(annotation -> TEST_ANNOTATION.equals(annotation.getType())))
                .map(method -> convertToTestCaseEvent(classDefItem, method)).forEach(testCaseEvents::add);

        return testCaseEvents;
    }

    @Nonnull
    private TestCaseEvent convertToTestCaseEvent(ClassDef classDefItem, Method method) {
        String testMethod = method.getName();
        Set<? extends Annotation> annotations = method.getAnnotations();

        String testClass = getClassName(classDefItem);

        boolean isIgnored = isClassIgnored(classDefItem) || isMethodIgnored(annotations);
        List<String> permissionsToRevoke = getPermissionsToRevoke(annotations);
        Map<String, String> properties = getTestProperties(annotations);
        return testCaseEvent()
                .withTestClass(testClass)
                .withTestMethod(testMethod)
                .withIsIgnored(isIgnored)
                .withPermissionsToRevoke(permissionsToRevoke)
                .withProperties(properties)
                .build();
    }

    private String getClassName(ClassDef classDefItem) {
        String typeDescriptor = classDefItem.getType();
        return typeDescriptor.substring(1, typeDescriptor.length() - 1).replace('/', '.');
    }

    private boolean isMethodIgnored(Set<? extends Annotation> annotations) {
        return containsAnnotation(IGNORE_ANNOTATION, annotations);
    }

    private List<String> getPermissionsToRevoke(Set<? extends Annotation> annotations) {
        return annotations.stream()
                .filter(annotationItem -> REVOKE_PERMISSION_ANNOTATION.equals(annotationItem.getType()))
                .flatMap(annotation -> (annotation.getElements())
                        .stream()).map(AnnotationElement::getValue)
                .flatMap(encodedValue -> ((ArrayEncodedValue) encodedValue).getValue().stream())
                .map(stringEncoded -> ((StringEncodedValue) stringEncoded).getValue()).collect(Collectors.toList());
    }

    private Map<String, String> getTestProperties(Set<? extends Annotation> annotations) {
        Map<String, String> properties = new HashMap<>();
        annotations.stream()
                .filter(annotationItem -> TEST_PROPERTIES_ANNOTATION.equals(annotationItem.getType()))
                .forEach(annotation -> {
                    Set<? extends AnnotationElement> elements = annotation.getElements();
                    List<String> keys = getAnnotationProperty(elements, "keys");
                    List<String> values = getAnnotationProperty(elements, "values");

                    for (int i = 0; i < min(values.size(), keys.size()); i++) {
                        properties.put(keys.get(i), values.get(i));
                    }
                });
        return properties;
    }

    private List<String> getAnnotationProperty(Set<? extends AnnotationElement> elements, String propertyName) {
        List<String> result = new ArrayList<>();
        for (AnnotationElement annotationElement : elements) {
            if (annotationElement.getName().equals(propertyName)) {
                ArrayEncodedValue array = (ArrayEncodedValue) annotationElement.getValue();
                for (EncodedValue value : array.getValue()) {
                    result.add(((StringEncodedValue) value).getValue());
                }
            }
        }

        return result;
    }

    private boolean isClassIgnored(ClassDef classDef) {
        Set<? extends Annotation> classAnnotations = classDef.getAnnotations();
        if (classAnnotations.isEmpty()) {
            return false;
        }
        return containsAnnotation(IGNORE_ANNOTATION, classAnnotations);
    }

    private boolean containsAnnotation(String comparisonAnnotation, Set<? extends Annotation> annotations) {
        return annotations.stream().anyMatch(annotation -> comparisonAnnotation.equals(annotation.getType()));
    }
}
