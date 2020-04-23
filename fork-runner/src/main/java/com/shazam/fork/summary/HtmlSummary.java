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
package com.shazam.fork.summary;

import java.util.Collection;
import java.util.List;

/**
 * Plain bean class, to feed to Moustache markup files.
 */
public class HtmlSummary {
    public Collection<HtmlPoolSummary> pools;
    public String title;
    public String subtitle;
    public List<String> ignoredTests;
    public String overallStatus;
    public List<String> failedTests;
    public List<String> fatalCrashedTests;
}
