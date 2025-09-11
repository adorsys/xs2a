#!/bin/bash
PSD_VERSION=$(grep -m1 -o 'version:.*' xs2a-impl/src/main/resources/static/psd2-api*.yaml | cut -d '"' -f2)
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
if [ -z "$BUILD_NUMBER" ]; then
  BUILD_NUMBER="local"
fi
sed -i "s|version:.*|version: \"$PSD_VERSION $PROJECT_VERSION Build:$BUILD_NUMBER\"|g" xs2a-impl/src/main/resources/static/psd2-api*.yaml
