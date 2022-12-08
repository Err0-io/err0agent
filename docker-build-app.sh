#!/bin/sh

rm -f docker/agent/*.jar && \
cp build/libs/err0agent-java_1_8-fat.jar docker/agent && \

docker build --no-cache docker/agent --tag err0_io:err0_agent && \
echo "Build OK"
