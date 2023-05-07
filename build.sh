#!/bin/bash

docker run \
-v /var/run/docker.sock:/var/run/docker.sock \
-v $PWD:/src \
-w /src \
maven:latest mvn spring-boot:build-image