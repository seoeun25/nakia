#!/usr/bin/env bash

if [[ "$TRAVIS_TAG" =~ ^v[0-9]+\.[0-9]+\.[0-9]+.* ]] && [ "$TRAVIS_EVENT_TYPE" == "push" ];
then
    export CONTAINER_PUSH=1
    export CONTAINER_VERSION=$(echo ${TRAVIS_TAG}|sed -e s/^v//g)
    GRADLE_ARGS="clean docker -xtest"
else
    echo "BB"
    GRADLE_ARGS="clean build -xdocker"
fi

echo "CONTAINER_PUSH: ${CONTAINER_PUSH}"
echo "CONTAINER_VERSION: ${CONTAINER_VERSION}"

pushd `dirname $0` > /dev/null
./gradlew ${GRADLE_ARGS}
popd > /dev/null
