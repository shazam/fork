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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Filter classes/packages under test.
 */
public class TestClassFilter {
    private static final Logger logger = LoggerFactory.getLogger(TestClassFilter.class);
    private final String filterPattern;

    public TestClassFilter(String filterPattern) {
        this.filterPattern = filterPattern;
    }

    public List<TestClass> anyUserFilter(List<TestClass> testClassesFromDexFile) {
		if (filterPattern == null) {
            return testClassesFromDexFile;
		}

        Set<TestClass> filteredIn = new HashSet<>();
        StringBuilder missingButSpecified = new StringBuilder();
        for (String filterIn : filterPattern.split(",")) {
            String filterInRegex = filterIn.contains(".*") ? filterIn : ".*" + filterIn + ".*";
            boolean matched = false;
            for (TestClass testClass : testClassesFromDexFile) {
                String testClassName = testClass.getName();
                if (testClassName.equals(filterIn) || testClassName.matches(filterInRegex)) {
                    filteredIn.add(testClass);
                    matched = true;
                }
            }
            if (!matched) {
                if (!"".equals(missingButSpecified.toString())) {
                    missingButSpecified.append(", ");
                }
                missingButSpecified.append(filterIn);
            }
        }
        if (missingButSpecified.length() > 0) {
            logger.warn("Filters specified but did not match any classes: " + missingButSpecified);
        }
        return new ArrayList<>(filteredIn);
	}
}
