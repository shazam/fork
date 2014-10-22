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
import com.shazam.fork.model.TestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists @Suppressed tests.
 */
public class SuppressedMethodFlagSetter implements DexMethodAnnotationVisitor {
    private static final Logger logger = LoggerFactory.getLogger(SuppressedMethodFlagSetter.class);
	private final TestClass testClass;

	public SuppressedMethodFlagSetter(TestClass testClass) {
		this.testClass = testClass;
	}

	public void visitAnnotation(String typeName, String methodName) {
		if ("Landroid/test/suitebuilder/annotation/Suppress;".equals(typeName)) {
			TestMethod method = testClass.getMethod(methodName);
			if (method != null) {
				method.setSuppressed();
			}
			else {
                logger.warn("Visit to {}.{} but method name not found", typeName, methodName);
			}
		}
	}

}
