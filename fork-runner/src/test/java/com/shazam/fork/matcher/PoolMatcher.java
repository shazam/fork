/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.matcher;

import com.shazam.fork.model.Pool;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.containsInAnyOrder;

public final class PoolMatcher {
    private PoolMatcher() {
    }

    @Nonnull
    public static Matcher<Pool> samePool(@Nonnull Pool expected) {
        return new TypeSafeDiagnosingMatcher<Pool>() {
            @Override
            protected boolean matchesSafely(Pool item, Description mismatchDescription) {
                boolean isNameMatched = item.getName().equals(expected.getName());
                if (!isNameMatched) {
                    String message = "expected a pool with name " + expected.getName() + ", but was " + item.getName();
                    mismatchDescription.appendText(message);
                    return false;
                }
                Object[] expectedDevices = expected.getDevices().toArray();
                boolean areDevicesMatched = containsInAnyOrder(expectedDevices).matches(item.getDevices());
                if (!areDevicesMatched) {
                    String message =
                            "expected a pool with devices " + expected.getDevices() + ", but was" + item.getDevices();
                    mismatchDescription.appendText(message);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with pool " + expected);
            }
        };
    }
}
