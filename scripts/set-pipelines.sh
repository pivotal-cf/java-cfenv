#!/usr/bin/env bash

set -euo pipefail

: "${FLY_TARGET:?'must be set'}"

main() {
  fly -t "$FLY_TARGET" sync

  pushd "$(dirname "$0")/../ci" > /dev/null
    echo "Setting java-cfenv pipeline..."
    fly --target "$FLY_TARGET" set-pipeline \
      --pipeline java-cfenv-4.x \
      --config pipeline.yml \
      --load-vars-from config-concourse.yml \
      --var branch=main
  popd > /dev/null
}

main
