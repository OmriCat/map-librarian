#!/bin/bash

set -eo pipefail

function bail() {
	echo "$@"
	exit 1
}

project_root="$(git rev-parse --show-toplevel)"

firestore_rules="$project_root/firestore.rules"
[[ -f "$firestore_rules" ]] || bail "File $firestore_rules doesn't exist"

project_name="map-librarian"

gradlew="$project_root/gradlew"
[[ -f "$gradlew" ]] || bail "Can't find gradle wrapper at $gradlew"

image_name="firebase-emulator-docker"

docker pull ghcr.io/grodin/"$image_name":latest

emulator="firebase-emulator"

function cleanup() {
  docker rm -f "$emulator" #> /dev/null 2>&1
}

trap cleanup SIGTERM SIGINT

function wait_for_emulator() {
  local health
  while [[ "$health" != "healthy" ]]
  do
    sleep 2
    health=$(docker inspect -f '{{.State.Health.Status}}' "$emulator")
    echo "$(date -Iseconds): $health"
  done
}

# Run cleanup just in case
cleanup

ui_port=4000
ui_websocket_port=9150
ui_logging_port=4500
hub_port=4400
auth_port=9099
firestore_port=8080

docker run \
    --detach \
    --interactive \
    --tty \
    --publish "$ui_port":"$ui_port" \
    --publish "$ui_websocket_port":"$ui_websocket_port" \
    --publish "$ui_logging_port":"$ui_logging_port" \
    --publish "$hub_port":"$hub_port" \
    --publish "$auth_port":"$auth_port" \
    --publish "$firestore_port":"$firestore_port" \
    --name "$emulator" \
    --volume "$firestore_rules:/home/firestore-emulator/firestore.rules" \
    "$image_name" \
 	  emulators:start --project "$project_name"

export -f wait_for_emulator
export emulator
timeout --foreground 60s  bash -c wait_for_emulator

CDPATH="" cd "$project_root"

"$gradlew" :integration-tests:firebase:allDevicesCheck

cleanup
