/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.io;

import org.jf.dexlib.DexFile;

import java.io.File;
import java.util.Collection;

import static java.util.Arrays.asList;

public class FakeDexFileExtractor implements DexFileExtractor {
    private DexFile[] dexFiles;

    private FakeDexFileExtractor() {
    }

    public static FakeDexFileExtractor fakeDexFileExtractor() {
        return new FakeDexFileExtractor();
    }

    public FakeDexFileExtractor thatReturns(DexFile... dexFiles) {
        this.dexFiles = dexFiles;
        return this;
    }

    @Override
    public Collection<DexFile> getDexFiles(File apkFile) {
        return asList(dexFiles);
    }
}
