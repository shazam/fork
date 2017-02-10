/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter.gradle;

import groovy.lang.Closure;

public class ForkJenkinsReportExtension {

    public String reportTitle;
    public String jenkinsUrl;
    public String jenkinsUsername;
    public String jenkinsPassword;
    public String jenkinsReportTitle;
    public FreestyleJob freestyleJob;
    public FolderJob folderJob;

    public void freestyleJob(Closure<?> freestyleJobClosure) {
        freestyleJob = new FreestyleJob();
        freestyleJobClosure.setDelegate(freestyleJob);
        freestyleJobClosure.call();
    }

    public void folderJob(Closure<?> folderJobClosure) {
        folderJob = new FolderJob();
        folderJobClosure.setDelegate(folderJob);
        folderJobClosure.call();
    }
}
