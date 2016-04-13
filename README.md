[![Build Status](https://travis-ci.org/shazam/fork.svg?branch=master)](https://travis-ci.org/shazam/fork)

Fork & Flakiness Reporter
===============
The Fork project consists of two libraries:

* [**Fork**](#fork) offers the fastest way to execute Android instrumentation test suites.
* [**Flakiness Reporter**](#flakiness-reporter) produces readable reports about test flakiness on tests suites previously executed by Fork.


# Fork
When running instrumentation tests, there is a significant time overhead for developers, especially on larger test suites. Existing solutions were not satisfactory for quick feedback before pushing code to VCS and for CI purposes.

We are big fans of [Spoon][1] and were using it for our plans, so we used it as our starting point. However, Spoon had similar issues to the Gradle and Maven test execution plugins, in the sense that it executes all tests on all of the connected devices (and emulators). We decided to tweak that naive scheduling to achieve much faster test execution.


## How it works
We introduced the notion of *pools of devices*. These are now responsible for running a test suite instead of each device running the suite separately. That has two side effects: 
* infinite scaling: your tests can speed up by as many devices and emulators as you can dedicate to your CI box. 
* because test suites now get scheduled to run on a pool, not all tests will run on all devices. For that reason, we also introduced a way to create a pool per device, which offers full coverage (a.k.a. Spoon-mode) but typically takes longer, so we run it on a nightly basis.
Fork works out-of-the-box, without any code changes.


## Running Fork
There are two ways to run Fork with your builds.

### Gradle plugin (recommended)
First, you need to add a build-script dependency:

```
buildscript {
    dependencies {
        classpath 'com.shazam.fork:fork-gradle-plugin:1.4.0-SNAPSHOT'
    }
}
```

Apply the Fork plugin
```
apply plugin: 'fork'
```

You're now done. You can enable smart pooling by adding runtime parameters (*Pooling and other parameters* section). If you had any instrumentation test tasks before, the plugin has added Fork tasks. You can verify by running:

```
gradlew tasks | grep fork
```

You can specify runtime configuration for Fork with the Fork DSL. Simply add a block to your build.gradle, _e.g.,_:

```groovy
fork {
    baseOutputDir "/my_custom_dir"
}
```

Property Name          | Property Type     | Default value
---------------------- | ----------------- | -------------
baseOutputDir          | File              | "fork"
testPackage            | String            | (Your instrumentation APK package)
testClassRegex         | String            | "^((?!Abstract).)*Test$"
testOutputTimeout      | int               | 60000
ignoreFailures         | boolean           | false
fallbackToScreenshots  | boolean           | true

### Standalone
Will potentially be unsupported, as it's the least developer friendly. Check out the Fork project and execute:

```
> gradlew fork-runner:run -Pargs='ARGUMENTS LIST'

With the below options.              The APK and test APK parameters are mandatory:
    --sdk                            Path to Android SDK. Defaults to the ANDROID_HOME environment variable.
    --apk                            Path to application. This parameter is required.
    --test-apk                       Path to test application. This parameter is required.
    --output                         Output path. Defaults to "fork-output"
    --test-package                   The package to consider when finding tests to run. Defaults to instrumentation package.
    --test-class-regex               Regex determining class names to consider when finding tests to run. Defaults to ^((?!Abstract).)*Test$
    --test-timeout                   The maximum amount of time during which the tests are allowed to not output any response, in milliseconds
    --fail-on-failure                Non-zero exit code on failure. Defaults to false.
    --fallback-to-screenshots        If a device does not support videos, define if you'd like animated GIFs (experimental). Defaults to true.
    --total-allowed-retry-quota      Total amount of allowed retries. If a test case fails and this quota hasn't been exhausted yet,
                                     the test case is scheduled to be executed again in the same device pool. Default to 0;
    --retry-per-test-case-quota      Amount of times a single test can be re-executed before declaring it a failure. Default to 1.
    --auto-grant-runtime-permissions Grants all runtime permissions in Android Marshmallow+. Default is true.
```

For example:
```
> gradlew fork-runner:run -Pargs='--apk /path/to/production.APK --test-apk /path/to/test.APK'

```

## Configuring pools and runtime
One of the most useful characteristics of the library is the way it creates the device pools. There are different options, to automatically create pools by API level, shortest width dimension and whether devices are self-described as tablets. On top of that, users can also manually create pools based on serial numbers, for maximum flexibility.

With either way of executing Fork (Gradle / standalone) you can specify how the pools are created by setting a combination of these environment variables:

One pooling option from:
* **fork.tablet=(true|false)** - to configure pools depending on their manufacturer's 'tablet' flag (ro.build.characteristics)
* **fork.pool.POOL_NAME=(Serial','?)** - to add devices with a given serial to a pool with given name,e.g. hdpi=01234567,abcdefgh
* **fork.computed.STRATEGY=(PoolName=LowerBound','?)** - to automatically create pools based on device characteristics, where
  * STRATEGY:=sw - by smallest width, e.g.phablet=0,tablet=720
  * STRATEGY:=api - by api, e.g. gingerbread_and_earlier=0,honeycomb_and_later=11)
* **fork.eachdevice=(true|false)** - to create a pool per device (a.k.a. Spoon mode). This is the default behaviour.

Any combination of other options from:
* **android.test.classes=REGEX** - comma separated regexes that specify a pattern for the classes/packages to run
* **fork.excluded.serial=(Serial','?)** - to exclude specific devices from running any tests
* **fork.report.title=Title** - to specify a title for the generated report
* **fork.report.subtitle=Subitle** - to specify a subtitle for the generated report
* **fork.test.size=(small|medium|large)** - to run test methods with the corresponding size annotation

*Note:* The Fork runtime parameter ```android.test.classes``` is applied _after_ both the ```testClassRegex``` and ```testPackage``` filters have been applied.

## Examples
A common case can be that you want to create two pools, one for phones & small tablets (7" and below) and one for large tablets. You could execute:
```
gradlew forkDebugTest -Dfork.computed.sw=phablets=0,tablets=720
```
The above will run tests on 2 pools, one named "phablets" and another called "tablets". The smallest width for the first pool will be 0 and for the latter 720 dpi.

## Diagnostics
(Example output to be provided)

## Limitations
 * The scheduling still works on a single build box with ADB, so there still is a limit by how many devices & emulators can be simultaneously connected to ADB. Eventually, Fork could be tweaked to talk over HTTP with other build agents, that would then be connected to devices over ADB. That model would tie in nicely with multi-agent CI systems, like Jenkins.

# Flakiness Reporter
One common problem with UI tests is the test flakiness from either the environment they run on or badly written tests. To help track down tests that are misbehaving, we are introducing the Flakiness reporter.

The reports produced by the Flakiness Reporter eventually make it trivial to find flaky tests and link to them and their diagnostics. Currently Jenkins is supported and it should be really easy to extend it to other types of CI servers.

## How it works
The Flakiness Reporter collects Fork output files, matches test runs over previous builds and sorts them according to their flakiness. Links are also created to each test of each test run, for easy navigation to diagnostics.

## Sample output
The output after a successful run of the Flakiness Reporter looks like the following:
![Fork Flakiness Reporter](static/flakiness.png)

## Running the Flakiness Reporter (Jenkins)
The Gradle plugin that allows the Reporter to run can be applied to a standalone project, since it doesn't directly depend on your Android project. For convenience, however, that is a good compromise.

Currently, the Reporter supports Jenkins but plugins can be written to be used with other CI servers.

To be able to use the Flakiness Reporter add these dependencies:
```
buildscript {
    dependencies {
        classpath "com.shazam.fork:fork-reporter-jenkins-gradle-plugin:1.3.0-SNAPSHOT"
    }
    repositories {
        maven { url "http://repo.jenkins-ci.org/public/" }
    }
}
```

Apply the Jenkins Flakiness Reporter plugin
```
apply plugin: 'fork-jenkins-gradle-plugin'
```

You can easily execute the Reporter with the following command.
```
gradlew forkJenkinsReport
```

### Gradle plugin configuration
To allow the Reporter communicate with your Jenkins server, you need to configure it with some basic details about your Jenkins Plan

Property Name          | Property Type  |  Description
---------------------- | -------------- | ---------------------------
reportTitle            | String         |  The title you want your report to have
jenkinsUrl             | String         |  The base URL of your Jenkins Server
jenkinsJobName         | String         |  The name of the job you want to be tracked
jenkinsReportTitle     | String         |  Optional, used to link to Fork diagnostics. [The report title you use to archive Fork's report folder](#publish-forks-html-report)

An example of a configuration:
```groovy
forkJenkins {
    reportTitle = "My project's awesome flakiness report"
    jenkinsUrl = "http://my-jenkins.server.net:8080/"
    jenkinsJobName = "Master"
    jenkinsReportTitle = "Fork Report"
 }
```

### Jenkins configuration
In your post-build action section in Jenkins, do the following two actions.

#### Archive Fork's summary file as an artifact
The Reporter works with summary files from Fork runs. For them to be accessible, they need to be archived like below:
![Fork summary archiving](static/archive-artifact.png)

#### Publish Fork's HTML report
This requires [Jenkins's HTML Publisher Plugin][2]. To be able to link to the right test runs, use a clear title.
![Fork HTML reports archiving](static/archive-html.png)

**Note:**The [forkJenkins.jenkinsReportTitle](#gradle-plugin-configuration) parameter of the gradle configuration has to match the Report Title added here.

#License

    Copyright 2015 Shazam Entertainment Limited.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


 [1]: https://github.com/square/spoon
 [2]: https://wiki.jenkins-ci.org/display/JENKINS/HTML+Publisher+Plugin

