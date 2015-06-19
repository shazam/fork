/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.listeners;

public class PoolProgressTracker {

    private final int totalTests;
    private int failedTests;
    private int completedTests;

    public PoolProgressTracker(int totalTests) {
        this.totalTests = totalTests;
    }

    void completedTest() {
        completedTests++;
    }

    void failedTest() {
        failedTests++;
    }

    float getProgress() {
        return (float) completedTests / (float) totalTests;
    }

    public int getNumberOfFailedTests() {
        return failedTests;
    }
}
