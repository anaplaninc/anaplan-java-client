#!/bin/bash

# Don't run the tests here, we want that to happen in skynet
BUILD_CMD="mvn package -Dmaven.test.skip"

if [ "${GIT_TAG}" ]
then
  RELEASE_VERSION=${GIT_TAG}
	echo "Detected tagged build version ${GIT_TAG}. Setting version to ${RELEASE_VERSION}"
	mvn versions:set -DnewVersion=${GIT_TAG}
else
	echo "No tag detected on the current commit. This is a non-release build."
fi

echo "Executing build as: ${BUILD_CMD}"
exit=0
eval "${BUILD_CMD}" || exit=$?

# RM currently requires all JAR files to contain a pom.xml for publishing; sources/javadocs don't (and shouldn't).
# BUILDTOOLS-3344 was closed with a disposition of 'Will Not Do'.
# For the forseeable future, need to inject a pom.xml into the javadocs jar
find ./target -name "anaplan-connect-*-*javadoc.jar" | xargs -I '{}' jar uvf {} pom.xml

exit $exit
