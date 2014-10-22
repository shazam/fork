/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork;

import com.shazam.fork.model.TestClass;
import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;

public class TestClassScanner {
    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("^((?!Abstract).)*Test;$");
    private static final String TEST = "test";
    private static final String CLASSES_DEX = "classes.dex";
    private final File instrumentationApkFile;
	private final File dexFileDestination;

	public TestClassScanner(File instrumentationApkFile, File output) {
		this.instrumentationApkFile = instrumentationApkFile;
		dexFileDestination = new File(output, CLASSES_DEX);
	}

	public List<TestClass> scanForTestClasses() {
		getDexClassesFromApk(instrumentationApkFile, dexFileDestination);
		return getTestClassesFromDexFile(dexFileDestination);
	}

    private static void getDexClassesFromApk(File apkFile, File dexFile) {
        ZipFile zip = null;
        InputStream classesDexInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            zip = new ZipFile(apkFile);
            ZipEntry classesDex = zip.getEntry(CLASSES_DEX);
            classesDexInputStream = zip.getInputStream(classesDex);
            fileOutputStream = new FileOutputStream(dexFile);
            copyLarge(classesDexInputStream, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(classesDexInputStream);
            closeQuietly(fileOutputStream);
            closeZipQuietly(zip);
        }
    }

    private static List<TestClass> getTestClassesFromDexFile(File dexFileDestination) {
        try {
            DexFile dexFile = new DexFile(dexFileDestination);
            List<ClassDefItem> items = dexFile.ClassDefsSection.getItems();
            List<TestClass> testClasses = new ArrayList<>();
            for (ClassDefItem classDefItem : items) {
                final String typeDescriptor = classDefItem.getClassType().getTypeDescriptor();
                if (TEST_CLASS_PATTERN.matcher(typeDescriptor).matches()) {
                    TestClass testClass = getTestClass(typeDescriptor);
                    testClasses.add(testClass);
                    try {
                        collectMethods(classDefItem, testClass);
                        visitMethodAnnotations(classDefItem,
                                new SuppressedMethodFlagSetter(testClass)
                        );
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
            return testClasses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void collectMethods(ClassDefItem classDefItem, TestClass testClass) {
        ClassDataItem classData = classDefItem.getClassData();
        if (classData == null) {
            return;
        }
        List<ClassDataItem.EncodedMethod> methods = classData.getVirtualMethods();
		for (ClassDataItem.EncodedMethod method : methods) {
			String methodName = method.method.getMethodName().getStringValue();
			if (methodName.startsWith(TEST)) {
				testClass.addMethod(methodName);
			}
		}
	}

	/**
	 * For the annotations known about by the class, find those relating to its methods, and walk them.
	 *
	 * @param classDefItem
	 * @param visitors
	 */
	private static void visitMethodAnnotations(ClassDefItem classDefItem, DexMethodAnnotationVisitor... visitors) {
		AnnotationDirectoryItem annotations = classDefItem.getAnnotations();
		List<AnnotationDirectoryItem.MethodAnnotation> methodAnnotations = annotations == null ? null : annotations
				.getMethodAnnotations();
		if (methodAnnotations != null) {
			for (AnnotationDirectoryItem.MethodAnnotation methodAnnotation : methodAnnotations) {
				String methodName = methodAnnotation.method.getMethodName().getStringValue();
				AnnotationItem[] set = methodAnnotation.annotationSet.getAnnotations();
				for (AnnotationItem annotationItem : set) {
					AnnotationEncodedSubValue encodedAnnotation = annotationItem.getEncodedAnnotation();
					String typeName = encodedAnnotation.annotationType.getTypeDescriptor();
					for (DexMethodAnnotationVisitor visitor : visitors) {
						visitor.visitAnnotation(typeName, methodName);
					}
				}
			}
		}
	}

	private static TestClass getTestClass(String typeDescriptor) {
		final String testClassName = typeDescriptor.substring(1, typeDescriptor.length() - 1).replace('/', '.');
		return new TestClass(testClassName);
	}

	private static void closeZipQuietly(ZipFile zip) {
		try {
			if (zip != null) {
				zip.close();
			}
		} catch (IOException e) {
		}
	}
}
