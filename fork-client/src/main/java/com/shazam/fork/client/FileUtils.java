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
package com.shazam.fork.client;

import java.io.*;

import static android.os.Environment.getExternalStorageDirectory;
import static com.shazam.fork.client.AndroidUtils.isMarshmallowOrAbove;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.copyOfRange;

public class FileUtils {

    private static final String TEST_CASE_CLASS_JUNIT_3 = "android.test.InstrumentationTestCase";
    private static final String TEST_CASE_METHOD_JUNIT_3 = "runMethod";
    private static final String TEST_CASE_CLASS_JUNIT_4 = "org.junit.runners.model.FrameworkMethod$1";
    private static final String TEST_CASE_METHOD_JUNIT_4 = "runReflectiveCall";
    private static final String TEST_CASE_CLASS_CUCUMBER_JVM = "cucumber.runtime.model.CucumberFeature";
    private static final String TEST_CASE_METHOD_CUCUMBER_JVM = "run";

    private static final File FORK_FOLDER = new File(getExternalStorageDirectory(), "fork");

    /**
     * Will save a copy of a file in fork folder
     *
     * @param folder A destination folder name
     * @param file   The file to save
     * @return The copy that was created.
     * @throws IOException
     */
    public static File saveFile(String folder, File file) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException("Can't find any file at: " + file);
        }

        StackTraceElement testClass = findTestClassTraceElement(currentThread().getStackTrace());
        String className = testClass.getClassName();
        String methodName = testClass.getMethodName();

        File targetFolder = concatFolder(FORK_FOLDER, folder, className, methodName);
        File target = new File(targetFolder, file.getName());

        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw new IOException("Unable to create output dir: " + targetFolder);
        }

        copy(file, target);
        return target;
    }

    private static File concatFolder(File baseFolder, String... folders) {
        if (folders == null || folders.length == 0) {
            return baseFolder;
        } else {
            return concatFolder(new File(baseFolder, folders[0]), copyOfRange(folders, 1, folders.length));
        }
    }

    public static void copy(File source, File target) throws IOException {
        if (!target.createNewFile()) {
            throw new IOException("Unable to create output file: " + target);
        }

        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(source));
        final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(target));
        byte[] buffer = new byte[4096];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }

        is.close();
        os.close();
    }

    private static StackTraceElement findTestClassTraceElement(StackTraceElement[] trace) {
        for (int i = trace.length - 1; i >= 0; i--) {
            StackTraceElement element = trace[i];
            if (TEST_CASE_CLASS_JUNIT_3.equals(element.getClassName())
                    && TEST_CASE_METHOD_JUNIT_3.equals(element.getMethodName())) {
                return extractStackElement(trace, i);
            }

            if (TEST_CASE_CLASS_JUNIT_4.equals(element.getClassName())
                    && TEST_CASE_METHOD_JUNIT_4.equals(element.getMethodName())) {
                return extractStackElement(trace, i);
            }
            if (TEST_CASE_CLASS_CUCUMBER_JVM.equals(element.getClassName())
                    && TEST_CASE_METHOD_CUCUMBER_JVM.equals(element.getMethodName())) {
                return extractStackElement(trace, i);
            }
        }

        throw new IllegalArgumentException("Could not find test class!");
    }

    private static StackTraceElement extractStackElement(StackTraceElement[] trace, int i) {
        //Stacktrace length changed in M
        return trace[isMarshmallowOrAbove() ? (i - 2) : (i - 3)];
    }

}
