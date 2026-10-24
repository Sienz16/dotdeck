#!/bin/sh
# Convenience entry point; Android Studio and ./gradlew work as well.
set -eu
cd "$(dirname "$0")"
if [ -z "${JAVA_HOME:-}" ]; then
    for candidate in "$HOME"/Library/Caches/dotdeck-tools/jdk-*/Contents/Home; do
        if [ -x "$candidate/bin/java" ]; then JAVA_HOME="$candidate"; export JAVA_HOME; break; fi
    done
fi
if [ "$#" -eq 0 ]; then set -- :app:coreTest :app:assembleDebug :app:assembleRelease :app:lintDebug; fi
exec ./gradlew "$@"
