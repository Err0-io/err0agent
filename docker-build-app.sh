#!/bin/sh

cp build/libs/err0agent-1.0.0-SNAPSHOT-fat.jar docker/client && \

docker build --no-cache docker/client --tag err0_io:err0_client && \
echo "Build OK"
