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
package com.shazam.fork.summary;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;
import java.util.Map;


@Root
class TestSuite {

	@ElementList(inline=true, type=TestCase.class, required=false)
	private List<TestCase> testCases;

    @Path("./properties")
    @ElementMap(required = false, entry = "property", key = "name", value = "value", attribute = true, inline = true)
    private Map<String, String> properties;

    public List<TestCase>  getTestCase() {
        return testCases;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
