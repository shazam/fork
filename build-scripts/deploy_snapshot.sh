#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype's snapshot repo.
#

REPOSITORY="shazam/fork"
BRANCH="master"

set -e

if [ "$GITHUB_REPOSITORY" != "$REPOSITORY" ]; then
  echo "Skipping snapshot deployment: wrong repository. Expected '$REPOSITORY' but was '$GITHUB_REPOSITORY'."
elif [ "$GITHUB_IS_PULL_REQUEST" != "false" ]; then
  echo "Skipping snapshot deployment: was pull request."
elif [ "$GITHUB_REF_NAME" != "$BRANCH" ]; then
  echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$GITHUB_REF_NAME'."
else
  echo "Deploying snapshot..."
  ./gradlew clean uploadArchives
  echo "Snapshot deployed!"
fi
