#!/usr/bin/env bash
# Build the Novvy Android debug APK.
#
# Requirements: JDK 17+ and an Android SDK (platforms;android-35, build-tools;35.0.0).
# Set ANDROID_HOME (or place sdk.dir in android/local.properties).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$ROOT/android"

# Prefer JDK 21 if the sdkman candidate is present.
if [ -d "/usr/local/sdkman/candidates/java/21.0.13.0+2-ms" ]; then
  export JAVA_HOME="/usr/local/sdkman/candidates/java/21.0.13.0+2-ms"
fi
export PATH="$JAVA_HOME/bin:$PATH"

if [ -n "${ANDROID_HOME:-}" ]; then
  echo "sdk.dir=$ANDROID_HOME" > "$ANDROID_DIR/local.properties"
fi

cd "$ANDROID_DIR"
./gradlew :app:assembleDebug --no-daemon

mkdir -p "$ROOT/dist"
cp "$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk" "$ROOT/dist/novvy.apk"
echo "APK written to: $ROOT/dist/novvy.apk"
ls -la "$ROOT/dist/novvy.apk"