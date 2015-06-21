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
package com.shazam.fork.pooling;

/**
 * Class holding lower bound and optional name
 */
public class Bound {
    private final int lower;
    private final String name;

    public Bound(int lower, String name) {
        this.lower = lower;
        this.name = name;
    }

    public int getLower() {
        return lower;
    }

    public String getName() {
        return name == null ? "" : (name + "=");
    }
}
