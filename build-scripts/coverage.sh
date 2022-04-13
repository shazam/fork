#!/bin/bash

set -e

./gradlew -q clean jacocoMergedReport
cd build-scripts
ls ../build/reports/jacoco/jacocoMergedReport
java -jar codacy-coverage-reporter-assembly-13.5.3.jar report -l Java -r "../build/reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml" -t "$CODACY_PROJECT_TOKEN"
