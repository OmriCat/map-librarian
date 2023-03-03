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

image_digest="sha256:b79d5aa006df16fb7ab74ef3aa1a33a95d9fcecb9dee0802a93cc99328b7bb77"
image_name="ghcr.io/grodin/firebase-emulator-docker@$image_digest"

container="firebase-emulator"

function cleanup() {
  docker rm -f "$container" #> /dev/null 2>&1
}

trap cleanup SIGTERM SIGINT

function wait_for_emulator() {
  local health
  while [[ "$health" != "healthy" ]]
  do
    sleep 2
    health=$(docker inspect -f '{{.State.Health.Status}}' "$container")
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
    --name "$container" \
    --volume "$firestore_rules:/home/firestore-emulator/firestore.rules" \
    "$image_name" \
 	  emulators:start --project "$project_name"

export -f wait_for_emulator
export container
timeout --foreground 120s  bash -c wait_for_emulator

echo "container_name=$container" >> "$GITHUB_OUTPUT"
