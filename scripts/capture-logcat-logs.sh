#!/bin/bash

set -o nounset

kill-child-jobs() {
  local jobs=$( jobs -p )
  echo "Killing $jobs"
  for pid in $jobs; do kill "$pid"; done
}

for sig in INT QUIT HUP TERM ALRM USR1; do
  trap "
    kill-child-jobs
    trap - $sig EXIT
    kill -s $sig "'"$$"' "$sig"
done
trap kill-child-jobs EXIT

project_dir="$(git rev-parse --show-toplevel)"

logcat_dir="$project_dir/build/logs"
mkdir -p "$logcat_dir"

logcat_logfile="$logcat_dir/logcat.log"

echo "Clearing logcat logs"
adb logcat -c

touch "$logcat_logfile"

echo "Logcat being written to $logcat_logfile"
adb logcat './*:D' > "$logcat_logfile" &

echo "Executing command '$*' in project dir $project_dir"

if (cd "$project_dir"; "$@") then
  exit 0
else
  exit 1
fi

kill-child-jobs

