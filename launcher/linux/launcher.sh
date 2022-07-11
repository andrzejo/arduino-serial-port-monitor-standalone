#!/usr/bin/env bash
#
# Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
# This is free software (GPL v.2).
#
# Copyright (c) Andrzej Oczkowicz 2022.
#

readonly dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
readonly path="$(find "${dir}" -iname "ArduinoSerialPortMonitorStandalone-*.jar" | head -n 1)"

if [[ -f "${path}" ]]; then
  exec java -jar "${path}"
else
  echo "Failed to find ArduinoSerialPortMonitorStandalone jar file."
fi
