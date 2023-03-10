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

image_digest="sha256:5960c62f090c124e425e792abd089dd892e7b6f404d17f50b893b28bf03e0c1d"
image_name="ghcr.io/grodin/firebase-emulator-docker@$image_digest"

container="firebase-emulator"
container_home_dir="/home/firebase-emulator"

function cleanup() {
  docker rm -f "$container" #> /dev/null 2>&1
}

trap cleanup SIGTERM SIGINT

function wait_for_emulator() {
  local health
  while [[ "$health" != "healthy" ]]; do
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
declare -a ports=("$ui_port" "$ui_logging_port" "$ui_websocket_port" "$hub_port" "$auth_port" "$firestore_port")
declare -a ports_args
for p in "${ports[@]}"; do
  ports_args+=("--publish" "${p}:${p}")
done

docker run \
  --detach \
  --interactive \
  --tty \
  --name "$container" \
  "${ports_args[@]}" \
  --mount type=bind,source="$firestore_rules",target=${container_home_dir}/firestore.rules,readonly \
  --mount target=${container_home_dir} \
  "$image_name" \
  emulators:start --project "$project_name"

TIMEOUT=${TIMEOUT:-120s}

export -f wait_for_emulator
export container
if ! timeout --foreground "${TIMEOUT}" bash -c wait_for_emulator; then
  bail "Timed out after ${TIMEOUT} waiting for emulator"
fi
