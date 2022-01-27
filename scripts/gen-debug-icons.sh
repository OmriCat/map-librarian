#!/bin/bash
set -e
shopt -s globstar

#function to find the first match for the first parameter in all parents of the current directory
function upfind() {
  dir="$(pwd)"
  while [ "$dir" != "/" ]; do
    path_to_find="$(find $dir/ -maxdepth 1 -name $1)"
    if [ ! -z $path_to_find ]; then
      echo "$path_to_find"
      return
    fi
    dir="$(dirname $dir)"
  done
}

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
echo "Script is in: $script_dir"
cd "$script_dir"
debug_file="$(readlink -f $(upfind debug.png))"

cd "$(dirname $(upfind gradlew))"
echo "Project base directory is $(pwd)"

for f in **/main/**/ic_launcher*.png ; do
  f_full=$(readlink -f "$f")
  target_file=$(echo "$f_full" | sed -e 's/main/debug/')
  target_dir=$(dirname "$target_file")
  if [ ! -d "$target_dir" ]; then
    echo "directory $target_dir doesn't exist, creating"
    mkdir -p "$target_dir"
  fi
  size=$(identify -format '%wx%h' "$f_full")
    # echo "$size"
  composite -verbose -geometry "$size" -gravity center "$debug_file" "$f_full" "$target_file"
  done
