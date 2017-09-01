Source code to generate tests.dex and app-debug.apk in the folder fork-common-test-dex.

1)Cd to the sub-folder `fork-common-test-dex`
2)run `./gradlew clean assemble assembleAndroidTest`
3)copy ./app/build/intermediates/transforms/dexMerger/androidTest/debug/0/classes.dex into the main module called `fork-common` in src/test/resources (and rename the file in tests.dex)
    aka: cp app/build/intermediates/transforms/dexMerger/androidTest/debug/0/classes.dex ../fork-common/src/test/resources/tests.dex
4)copy ./app/build/outputs/apk/debug/app-debug.apk into the main module called `fork-common` in src/test/resources
    aka: cp app/build/outputs/apk/debug/app-debug.apk ../fork-common/src/test/resources
5)Cd back to `fork`
6)run at least once `./gradlew test`
