#!/usr/bin/env sh
# Re-renders the README screenshots from the real UI on demo data (needs a desktop session, not headless).
set -e
cd "$(dirname "$0")/.."
./mvnw -q -B test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt -Dmdep.includeScope=test
java -cp "target/test-classes:target/classes:$(cat target/cp.txt)" com.campuscrib.ui.ScreenshotTour docs/images
