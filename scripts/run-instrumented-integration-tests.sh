#!/bin/bash

set -o nounset

bail() {
  local err_code=$1
  shift
  echo "Error: $*"
  exit "$err_code"
}

project_dir="$(git rev-parse --show-toplevel)"

echo "Clearing logcat logs"
adb logcat -c

logcat_dir="$project_dir/build/logs"
mkdir -p "$logcat_dir" || bail 11 "Couldn't create directory $logcat_dir"

logcat_logfile="$logcat_dir/logcat.log"
touch "$logcat_logfile"

echo "Logcat being written to $logcat_logfile"
adb logcat './*:D' > "$logcat_logfile" &
logcat_pid="$!"

exit-kill-logcat() {
  echo "Killing adb logcat process with PID $logcat_pid"
  kill -TERM "$logcat_pid"
  exit "$1"
}

trap 'exit-kill-logcat 20' INT TERM

adb_cmd="adb shell am instrument -w com.omricat.maplibrarian.integrationtesting.debug/androidx.test.runner.AndroidJUnitRunner"
echo "Executing command $adb_cmd in project dir $project_dir"
cd "$project_dir" || bail 10 "Couldn't cd into $project_dir"
$adb_cmd
exit-kill-logcat "$?"
