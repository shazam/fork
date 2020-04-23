/*
 * Copyright 2019 Apple Inc.
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
package com.shazam.fork.model;

/**
 * Represents the pixel size of the screen, smaller dimension first.
 */
public class DisplayGeometry {

	private final int small;
	private final int large;
	private final double density;

	public DisplayGeometry(int d1, int d2, double density) {
		this.density = density;
		small = (d1 > d2) ? d2 : d1;
		large = (d1 > d2) ? d1 : d2;
	}

	public DisplayGeometry(Integer sw) {
		small = sw;
		large = 0;
		density = 1;
	}

	@Override
	public String toString() {
		return String.format("[%dx%d]/%3.3f (sw=%3.3f=%d)", small, large, density, small/density, getSwDp());
	}

	public int getSwDp() {
		return (int)(small / density);
	}

	public boolean matches(DisplayGeometry displayGeometry) {
        return small == displayGeometry.small && large == displayGeometry.large && density == displayGeometry.density;
	}
}
