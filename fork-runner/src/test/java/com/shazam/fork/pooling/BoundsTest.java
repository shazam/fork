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

import com.shazam.fork.model.Device;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BoundsTest {

	private final ComputedPoolingStrategy dummyNameOnly = new ComputedPoolingStrategy() {
		@Override
		public boolean canPool(Device device) {
			return true;
		}

		@Override
		public int getParameter(Device device) {
			return 0;
		}

		@Override
		public String getBaseName() {
			return "dummy";
		}

		@Override
		public String help() {
			return null;
		}
	};

	private final Bound[] bound = new Bound[] {new Bound(1, "OneToTen"), new Bound(11, "elevenToTwenty"), new Bound(21, "TwentyOneUp")};
	private final Bounds bounds = new Bounds(bound);

	@Test
	public void testItemOnLowerBoundMakesItsCategory() {
		int index = bounds.findEnclosingBoundIndex(11);
		String name = bounds.getName(index, dummyNameOnly);
		assertThat(name, equalTo("elevenToTwenty=dummy11-20"));
	}

	@Test
	public void testItemOnUpperBoundMakesItsCategory() {
		int index = bounds.findEnclosingBoundIndex(10);
		String name = bounds.getName(index, dummyNameOnly);
		assertThat(name, equalTo("OneToTen=dummy1-10"));
	}

	@Test
	public void testItemAboveAllBoundsMakesFinalCategory() {
		int index = bounds.findEnclosingBoundIndex(100);
		String name = bounds.getName(index, dummyNameOnly);
		assertThat(name, equalTo("TwentyOneUp=dummy21-up"));
	}

	@Test
	public void testItemBelowAllBoundsMakesInitialCategory() {
		int index = bounds.findEnclosingBoundIndex(0);
		String name = bounds.getName(index, dummyNameOnly);
		assertThat(name, equalTo("dummy0-0"));
	}
}
