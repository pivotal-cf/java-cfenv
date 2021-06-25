#!/usr/bin/env bash

set -euo pipefail

readonly FLY_TARGET="scs"

main() {
  fly -t "$FLY_TARGET" sync

  pushd "$(dirname "$0")/../ci" > /dev/null
    echo "Setting java-cfenv pipeline..."
    fly --target "$FLY_TARGET" set-pipeline \
      --pipeline java-cfenv \
      --config pipeline.yml \
      --load-vars-from config-concourse.yml \
      --var branch=main
  popd > /dev/null
}

main
