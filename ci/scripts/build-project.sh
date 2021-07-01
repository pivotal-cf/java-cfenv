#!/bin/bash

set -euo pipefail

readonly ONLY_SHOW_STANDARD_STREAMS_ON_TEST_FAILURE="${ONLY_SHOW_STANDARD_STREAMS_ON_TEST_FAILURE:-true}"
readonly SKIP_TESTS="${SKIP_TESTS:-false}"

# shellcheck source=common.sh
source "$(dirname "$0")/common.sh"
repository=$(pwd)/distribution-repository
if [ "$SKIP_TESTS" == "true" ]; then
	build_task=assemble
else
	build_task=build
fi

pushd git-repo >/dev/null
./gradlew --parallel clean "$build_task" publish \
	-PonlyShowStandardStreamsOnTestFailure="${ONLY_SHOW_STANDARD_STREAMS_ON_TEST_FAILURE}" \
	-PpublicationRepository="${repository}"
popd >/dev/null
