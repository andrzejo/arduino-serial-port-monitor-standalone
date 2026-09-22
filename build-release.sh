#!/usr/bin/env bash
#
# Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
# This is free software (GPL v.2).
#
# Copyright (c) Andrzej Oczkowicz 2022.
#
set -eo pipefail

[[ "${TRACE}" ]] && set -x

readonly dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "${dir}"

mvn -Dbuild.setVersion=1.0.1 clean package -DskipTests
mvn assembly:single

readonly version="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
#git tag -a "${version}" -m "Release ${version}"
