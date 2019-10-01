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
package com.shazam.fork.pooling;

import com.google.common.collect.ImmutableMap;
import com.shazam.fork.ComputedPooling;
import com.shazam.fork.model.Device;

import org.junit.Test;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ComputedPoolsCategorizerTest {

	private final ComputedPooling computedPooling = new ComputedPooling() {{
		characteristic = Characteristic.api;
		groups = ImmutableMap.of(
				"OneToTen", 1,
				"elevenToTwenty", 11,
				"TwentyOneUp", 21
		);
	}};

	private final ComputedPoolsCategorizer computedPoolsCategorizer = new ComputedPoolsCategorizer(computedPooling);

	@Test
	public void testItemOnLowerBoundMakesItsCategory() {
        assertPoolWithApiHasName("11", "elevenToTwenty=api11-20");
	}

    @Test
	public void testItemOnUpperBoundMakesItsCategory() {
        assertPoolWithApiHasName("10", "OneToTen=api1-10");

	}

	@Test
	public void testItemAboveAllBoundsMakesFinalCategory() {
        assertPoolWithApiHasName("100", "TwentyOneUp=api21-up");
	}

	@Test
	public void testItemBelowAllBoundsMakesInitialCategory() {
        assertPoolWithApiHasName("0", "api0-0");
	}

    private void assertPoolWithApiHasName(String apiLevel, String poolName) {
        String poolForDevice = computedPoolsCategorizer.poolForDevice(deviceWithApi(apiLevel));
        assertThat(poolForDevice, equalTo(poolName));
    }

	private Device deviceWithApi(String apiLevel) {
		return aDevice()
				.withApiLevel(apiLevel)
				.build();
	}
}
