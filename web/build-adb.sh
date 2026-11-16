#!/usr/bin/env bash
# Rebuild web/adb.js (browser ADB over WebUSB) from web/src/app.js.
set -euo pipefail
cd "$(dirname "$0")"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
cp src/app.js "$WORK/app.js"
cd "$WORK"
npm init -y >/dev/null 2>&1
npm install --silent @yume-chan/adb @yume-chan/adb-daemon-webusb @yume-chan/adb-credential-web esbuild >/dev/null 2>&1
./node_modules/.bin/esbuild app.js --bundle --format=iife --global-name=NovvyAdb \
  --platform=browser --target=es2020 --outfile="$OLDPWD/adb.js"
echo "Built $(dirname "$0")/adb.js"
