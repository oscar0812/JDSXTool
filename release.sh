#!/bin/bash

# Check for version argument
if [ -z "$1" ]; then
  echo "Usage: ./release.sh <version>"
  exit 1
fi

VERSION=$1

# Stage all uncommitted changes
git add .

# Commit changes
echo "Enter a commit message: "
read COMMIT_MSG
if [ -z "$COMMIT_MSG" ]; then
  COMMIT_MSG="Release version $VERSION"
fi

git commit -m "$COMMIT_MSG"

# Update version in build.gradle
if grep -q "version = '" build.gradle; then
  sed -i "s/version = '.*'/version = '$VERSION'/" build.gradle
else
  echo "Version property not found in build.gradle!"
  exit 1
fi

# Commit version change
git add build.gradle
git commit -m "Bump version to $VERSION"

# Tag the release
git tag $VERSION

# Push commits and tag
git push origin main
git push origin $VERSION

echo "Successfully pushed release $VERSION to GitHub!"
