#!/bin/bash

set -e

sudo curl https://www.jpm4j.org/install/script | sh
jpm install com.codacy:codacy-coverage-reporter:assembly
./gradlew -q clean jacocoMergedReport
codacy-coverage-reporter -l Java -r build/reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml
