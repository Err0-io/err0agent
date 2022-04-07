#!/bin/sh

docker pull err0io/agent:latest && \
docker run --mount type=bind,source=`pwd`,destination=/mnt err0io/agent:latest \
  /usr/local/bin/err0.sh --token /mnt/tokens/err0-err0-io-err0agent-dc4ccb4c-5da5-11ec-a0b8-4622287bbd85.json --insert /mnt