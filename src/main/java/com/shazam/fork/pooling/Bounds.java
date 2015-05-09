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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Ordered list of Bound objects.
 */
public class Bounds {
    private final List<Bound> bounds;

    public Bounds(Bound[] boundArray) {
        bounds = asList(boundArray);
    }

    public String getName(int i, ComputedPoolingStrategy computedPoolingStrategy) {
        Bound lower = (i == 0) ? new Bound(0, null) : bounds.get(i - 1);
        return lower.getName() + computedPoolingStrategy.getBaseName() + lower.getLower()
                + ((i < bounds.size()) ? "-" + (bounds.get(i).getLower() - 1) : "-up");
    }

    public int findEnclosingBoundIndex(int parameter) {
        int i = 0;
        while (i < bounds.size() && parameter >= bounds.get(i).getLower()) {
            ++i;
        }
        return i;
    }

    public Collection<String> allNames(ComputedPoolingStrategy computedPoolingStrategy) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= bounds.size(); ++i) {
            list.add(getName(i, computedPoolingStrategy));
        }
        return list;
    }

}
