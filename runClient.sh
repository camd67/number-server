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
  mvn package -DskipTests
  echo
  echo
fi

java -jar target/number-client.jar "$@"