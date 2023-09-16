#!/bin/bash
set -o errexit -o nounset
adb shell am instrument -w com.omricat.maplibrarian.integrationtesting.debug/androidx.test.runner.AndroidJUnitRunner
