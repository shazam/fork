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
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.Log;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatTimestamp;
import com.android.ddmlib.testrunner.TestIdentifier;

import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LogCatSerializerTest {
    public static final String CLASS1_NAME = "com.test.waffles.WaffleTest";
    public static final String CLASS1d_NAME = "com.testXwaffles.WaffleTest";
    public static final String CLASS2_NAME = "com.test.waffles.CrumpetTest";
    public static final String TEST1_NAME = "testBadgersLikeWaffles";
    public static final String TEST2_NAME = "testBadgersLikeWafflesLikeTimeFliesLikeABanana";
    private long milli;

    @SuppressWarnings("unchecked")
    @Test
    public void testOnlySerialisesTestSpecificLogs() {
        TestIdentifier test = new TestIdentifier(CLASS1_NAME, TEST1_NAME);
        SpyLogCatWriter logCatWriter = new SpyLogCatWriter();
        LogCatSerializer serializer = new LogCatSerializer(test, logCatWriter);

        List<LogCatMessage> logCatMessages = new ArrayList<>();
        addTestStartEnd(logCatMessages, TEST2_NAME, CLASS1_NAME);
        addTestStartEnd(logCatMessages, TEST1_NAME, CLASS1_NAME);
        addTestStartEnd(logCatMessages, TEST2_NAME, CLASS2_NAME);
        addTestStartEnd(logCatMessages, TEST1_NAME, CLASS2_NAME);
        serializer.serializeLogs(logCatMessages);

        MatcherAssert.assertThat(logCatWriter.logCatMessages, IsIterableContainingInOrder.contains(
                logCatMessageWithString(startedMessage(TEST1_NAME, CLASS1_NAME)),
                logCatMessageWithString(finishedMessage(TEST1_NAME, CLASS1_NAME))
        ));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testIsNotFooledByUnlikelyPackageNameClashes() {
        TestIdentifier test = new TestIdentifier(CLASS1_NAME, TEST1_NAME);
        SpyLogCatWriter logCatWriter = new SpyLogCatWriter();
        LogCatSerializer serializer = new LogCatSerializer(test, logCatWriter);

        List<LogCatMessage> logCatMessages = new ArrayList<>();
        addTestStartEnd(logCatMessages, TEST2_NAME, CLASS1_NAME);
        addTestStartEnd(logCatMessages, TEST1_NAME, CLASS1_NAME);
        addTestStartEnd(logCatMessages, TEST2_NAME, CLASS1d_NAME);
        addTestStartEnd(logCatMessages, TEST1_NAME, CLASS1d_NAME);
        serializer.serializeLogs(logCatMessages);

        MatcherAssert.assertThat(logCatWriter.logCatMessages, IsIterableContainingInOrder.contains(
                logCatMessageWithString(startedMessage(TEST1_NAME, CLASS1_NAME)),
                logCatMessageWithString(finishedMessage(TEST1_NAME, CLASS1_NAME))
        ));
    }

    private org.hamcrest.Matcher<LogCatMessage> logCatMessageWithString(final String expected) {
        return new TypeSafeDiagnosingMatcher<LogCatMessage>() {
            @Override
            protected boolean matchesSafely(LogCatMessage actual, Description but) {
                but.appendText("had message '" + actual.getMessage() + "'");
                return actual.getMessage().equals(expected);
            }

            @Override
            public void describeTo(Description expecting) {
                expecting.appendText("with message '" + expected + "'");
            }
        };
    }


    private void addTestStartEnd(List<LogCatMessage> logCatMessages, String testName, String className) {
        logCatMessages.add(nextMessage(startedMessage(testName, className)));
        logCatMessages.add(nextMessage(finishedMessage(testName, className)));
    }

    private String finishedMessage(String testName, String className) {
        return "finished: " + testName + "(" + className + ")";
    }

    private String startedMessage(String testName, String className) {
        return "started: " + testName + "(" + className + ")";
    }

    private LogCatMessage nextMessage(String msg) {
        String timestampString = String.valueOf("08-13 22:37:24." + ++milli);
        return new LogCatMessage(Log.LogLevel.INFO, 1234, 4321, "TestRunner", "TestRunner",
                LogCatTimestamp.fromString(timestampString), msg);
    }

    private static class SpyLogCatWriter implements LogCatWriter {
        private List<LogCatMessage> logCatMessages;

        @Override
        public void writeLogs(TestIdentifier test, List<LogCatMessage> logCatMessages) {
            this.logCatMessages = logCatMessages;
        }
    }
}
