#!/bin/sh
# Portable launcher: requires a system-installed Java 17+, ships no bundled JRE.
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$DIR/CVC-Viewer.jar" "$@"
