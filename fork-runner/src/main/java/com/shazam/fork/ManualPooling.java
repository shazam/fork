/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork;

import groovy.lang.Closure;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ManualPooling implements Serializable {
    public Map<String, Collection<String>> groupings;

    public ManualPooling() {
    }

    public ManualPooling(Map<String, Collection<String>> groupings) {
        this.groupings = groupings;
    }

    public void groupings(Closure<?> groupingsClosure) {
        groupings = new HashMap<>();
        groupingsClosure.setDelegate(groupings);
        groupingsClosure.call();
    }
}
