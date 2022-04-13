#!/bin/bash
#
# Run tests for all modules
#

set -e

echo "Running tests..."
./gradlew test
