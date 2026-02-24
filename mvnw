#!/bin/sh
# Maven Wrapper Script
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
# ----------------------------------------------------------------------------

set -e

# OS specific support.
cygwin=false
darwin=false
mingw=false
msys=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  MINGW*) mingw=true ;;
  MSYS*) msys=true ;;
esac

# Determine the Java executable
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
  if [ ! -x "$JAVA_CMD" ]; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVA_CMD="java"
  which java > /dev/null 2>&1 || { echo "ERROR: JAVA_HOME is not set and 'java' is not on PATH." >&2; exit 1; }
fi

# Determine the location of this script
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-}"
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
fi

WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -f "$WRAPPER_PROPERTIES" ]; then
  DISTRIBUTION_URL=$(grep '^distributionUrl=' "$WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')
  WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" | sed 's/wrapperUrl=//')
fi

# Download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "ERROR: curl or wget required to download Maven Wrapper." >&2
    exit 1
  fi
fi

exec "$JAVA_CMD" -jar "$WRAPPER_JAR" "$MAVEN_PROJECTBASEDIR" "$DISTRIBUTION_URL" "$@"
