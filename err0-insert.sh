#!/bin/sh

docker pull err0io/agent:latest && \
docker run --mount type=bind,source=`pwd`,destination=/mnt err0io/agent:latest \
  /usr/local/bin/err0.sh --token /mnt/tokens/err0agent.json --insert /mnt
