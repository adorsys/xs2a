#!/bin/bash
PSD_VERSION=$(grep -o 'version:.*' xs2a-impl/src/main/resources/static/psd2-api*.yaml  | cut -d \" -f2)
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
sed -i "s/version:.*/version: \"$PSD_VERSION $PROJECT_VERSION Build:$BUILD_NUMBER\"/g"  xs2a-impl/src/main/resources/static/psd2-api*.yaml
