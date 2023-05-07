#!/bin/bash

docker run \
-v /var/run/docker.sock:/var/run/docker.sock \
-v $PWD:/src \
-w /src \
buildpacksio/pack build test --builder=paketobuildpacks/builder:full