#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ -z "${JAVA_HOME:-}" ]; then
    for candidate in "$HOME"/Library/Caches/dotdeck-tools/jdk-*/Contents/Home; do
        if [ -x "$candidate/bin/javac" ]; then JAVA_HOME="$candidate"; break; fi
    done
fi
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
mkdir -p app/build/core-tests
"$JAVAC" --release 8 -d app/build/core-tests \
    $(find app/src/main/java/app/dotdeck/launcher/core app/src/test/java -name '*.java')
"$JAVA" -cp app/build/core-tests app.dotdeck.launcher.core.CoreTest
