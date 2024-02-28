#!/usr/bin/env bash
#
# Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
# This is free software (GPL v.2).
#
# Copyright (c) Andrzej Oczkowicz 2022.
#

readonly LAUNCHER_VERSION=1.0.1

readonly grn='\033[0;32m'
readonly red='\033[0;31m'
readonly bld='\e[1m'
readonly nc='\033[0m'

readonly url="http://localhost:4255/api/"
readonly dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
readonly path="$(find "${dir}" -iname "ArduinoSerialPortMonitorStandalone-*.jar" | head -n 1)"

echo -e "${grn}Arduino Serial Port Monitor - Standalone${nc}, Linux Launcher ver. ${red}${LAUNCHER_VERSION}${nc}\n"

if [[ -n "${1}" ]]; then
  readonly method=${1}
  readonly res=${2}
  readonly body=${3}
  readonly ignoreError=
  if [[ -z "${res}" ]]; then
    echo -e "Api usage:\n${0} ${grn}method${nc}(get|post) ${grn}resource${nc} [${grn}body${nc}] [--ignore-error]\n"
    exit 1
  fi
  echo -e "Execute API request ${bld}${method}${nc} ${bld}${res}${nc} to ${grn}${url}${nc}\n"
  if [[ -n "${body}" ]]; then
    readonly bodyParam="-d ${body}"
  fi
  # shellcheck disable=SC2086
  curl -X "${method}" "${url}${res}" ${bodyParam}
  readonly exitCode=$?
  if [[ $* == *"--ignore-error"* ]]; then
    exit 0
  else
    exit $exitCode
  fi
fi

if command -v java >/dev/null 2>&1; then
  javaVersion=$(java -version 2>&1 | head -n 1 | tr -d "\n")
else
  echo -e "${red}Java executable not found! Please install Java!${nc}\n"
  exit 1
fi

if [[ -f "${path}" ]]; then
  echo -e "Running app JAR file using ${grn}Java ${javaVersion}${nc}: ${bld}${path}${nc}\n"
  exec java -jar "${path}"
else
  echo -e "${red}Failed to find ArduinoSerialPortMonitorStandalone jar file${nc}.\n"
fi
