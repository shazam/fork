Fork
====

Fork is the fastest way to execute Android instrumentation test suites.


Description
-----------

When running instrumentation tests, there is a significant time overhead for developers, especially on larger test suites. Existing solutions were not satisfactory for quick feedback before pushing code to VCS and for CI purposes.

We are big fans of [Spoon][1] and were using it for our plans, so we used it as our starting point. However, Spoon had similar issues to the Gradle and Maven test execution plugins, in the sense that it executes all tests on all of the connected devices (and emulators). We decided to tweak that naive scheduling to achieve much faster test execution.


How it works
------------
We introduced the notion of *pools of devices*. These are now responsible for running a test suite instead of each device running the suite separately. That has two side effects: 
* infinite scaling: your tests can speed up by as many devices and emulators as you can dedicate to your CI box. 
* because test suites now get scheduled to run on a pool, not all tests will run on all devices. For that reason, we also introduced a way to create a pool per device, which offers full coverage (a.k.a. Spoon-mode) but typically takes longer, so we run it on a nightly basis.

Fork works out-of-the-box, without any code changes.



Pooling
-------

One of the most useful characteristics of the library is the way it creates the device pools. There are different options, to automatically create pools by API level, shortest width dimension and whether devices are self-described as tablets. On top of that, users can also manually create pools based on serial numbers, for maximum flexibility.



How to run Fork
---------------

There are two ways to run Fork with your builds.

### [Gradle plugin](https://github.com/shazam/fork-gradle-plugin) (recommended)
This is the only way that will be supported in the future. First, you need to add a build-script dependency:

```
buildscript {
    dependencies {
        classpath 'com.shazam.fork:fork-gradle-plugin:0.9.0-SNAPSHOT'
    }
}
```

Apply the Fork plugin
```
apply plugin: 'fork'
```

You're now done. You can enable smart pooling by adding runtime parameters (*Pooling and other parameters* section). If you had any instrumentation test tasks before, the plugin has added Fork tasks. You can verify by running:

```
gradlew tasks #(and optionally | grep fork)
```
### Maven plugin (work in progress)
Watch this space.

### Standalone
Will potentially be unsupported, as it's the least developer friendly. Check out the Fork project and execute:

```
> gradlew run

With the below options. The APK and test APK parameters are mandatory:
    --apk               Application
    --test-apk          Test application APK
    --output            Output path
    --fail-on-failure   Non-zero exit code on failure
    --sdk               Path to Android SDK
```


Pooling and other parameters
----------------------------

With either way of executing Fork (Gradle/standalone/maven) you can specify these common runtime parameters:

One of:
 * **fork.tablet=(true|false)** - to configure pools depending on their manufacturer's 'tablet' flag (ro.build.characteristics)
 * **fork.pool.POOL_NAME=(Serial','?)\* ** - to add devices with a given serial to a pool with given name,e.g. hdpi=01234567,abcdefgh
 * **fork.computed.STRATEGY=(PoolName=LowerBound','?)\* ** - to automatically create pools based on device characteristics, where
 
	STRATEGY:=sw - by smallest width, e.g.phablet=0,tablet=720

	STRATEGY:=api - by api, e.g. gingerbread_and_earlier=0,honeycomb_and_later=11)
 * **fork.eachdevice=(true|false)** - to create a pool per device (a.k.a. Spoon mode). This is the default behaviour.

Any of:
* **android.test.classes=REGEX** - to specify a pattern for the classes/packages to run
* **fork.excluded.serial=(Serial','?)\* ** - to exclude specific devices from running any tests
* **fork.report.title=Title** - to specify a title for the generated report
* **fork.report.subtitle=Subitle** - to specify a subtitle for the generated report

Limitations
-----------
 * Multi-dex test APKs are not supported yet.
 * The scheduling still works on a single build box with ADB, so there still is a limit by how many devices & emulators can be simultaneously connected to ADB. Eventually, Fork could be tweaked to talk over HTTP with other build agents, that would then be connected to devices over ADB. That model would tie in nicely with multi-agent CI systems, like Jenkins.


 [1]: https://github.com/square/spoon