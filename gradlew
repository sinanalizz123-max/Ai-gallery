#!/usr/bin/env sh

set -e

DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

CLASSPATH="$DIR/gradle/wrapper/gradle-wrapper.jar:$DIR/gradle/wrapper/gradle-wrapper-shared.jar"

exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
