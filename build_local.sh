#!/usr/bin/env bash

## build. create docker image and push to docker repo(docker.lezhin.com)
help="./build_local.sh <version>"

VERSION=$1
if [ "$VERSION" == "--help" ]
then
      echo "$help"
      exit 0;
fi

CMD="CONTAINER_VERSION=${VERSION} CONTAINER_PUSH=1 gradle docker"
echo "$CMD"
echo ""

CONTAINER_VERSION=${VERSION} CONTAINER_PUSH=1 gradle docker

