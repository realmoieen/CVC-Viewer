#!/bin/bash
# Portable launcher: requires a system-installed Java 17+, ships no bundled JRE.
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$DIR/../Resources/app/CVC-Viewer.jar" "$@"
