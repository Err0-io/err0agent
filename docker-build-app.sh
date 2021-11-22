#!/bin/sh

cp build/libs/err0agent-1.0.0-SNAPSHOT-fat.jar docker/agent && \

docker build --no-cache docker/agent --tag err0_io:err0_agent && \
echo "Build OK"
