/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.chimprunner.injector.io;

import com.shazam.fork.io.DexFileExtractor;

import static com.shazam.chimprunner.injector.ConfigurationInjector.configuration;

public class DexFileExtractorInjector {

    public static DexFileExtractor dexFileExtractor() {
        return new DexFileExtractor(configuration().getOutput());
    }
}
