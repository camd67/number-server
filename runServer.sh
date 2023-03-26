#!/usr/bin/env bash

set -euo pipefail

if ! [[ $(command -v mvn) ]]; then
  echo "Missing maven. Please install it and add it to your path."
  exit 1
fi

if ! [[ $(command -v java) ]]; then
  echo "Missing java. Please install it and add it to your path."
  exit 1
fi

if [[ -z ${SKIP_COMPILE:-} ]]; then
  mvn package -DskipTests -Pserver
  echo
  echo
fi

DEBUG_ARGS=""
if [[ ${1:-''} == '-d' ]]; then
  DEBUG_ARGS='-agentlib:jdwp=transport=dt_socket,server=y,address=5005,suspend=n'
fi

java $DEBUG_ARGS -jar target/number-server.jar "$@"