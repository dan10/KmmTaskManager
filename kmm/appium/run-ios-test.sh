#!/bin/bash
set -e

# iOS Performance Test Runner
# This script runs the performance metrics test on iOS simulator

echo "🍎 iOS Performance Test Runner"
echo "================================"
echo ""

# Check if UDID is provided
if [ -z "$1" ]; then
    echo "❌ Error: Simulator UDID required"
    echo ""
    echo "Usage: ./run-ios-test.sh <SIMULATOR_UDID> [BUNDLE_ID]"
    echo ""
    echo "Example:"
    echo "  ./run-ios-test.sh 44D6FB9B-02A2-41AA-986A-23D70977E508"
    echo ""
    echo "To find your simulator UDID:"
    echo "  xcrun simctl list devices available | grep 'iPhone'"
    echo ""
    exit 1
fi

UDID="$1"
BUNDLE_ID="${2:-com.danioliveira.taskmanager.KmmTaskManager}"

echo "📱 Simulator UDID: $UDID"
echo "📦 Bundle ID: $BUNDLE_ID"
echo ""

# Check if simulator is booted
echo "🔍 Checking simulator status..."
BOOTED=$(xcrun simctl list devices | grep "$UDID" | grep "Booted" || echo "")
if [ -z "$BOOTED" ]; then
    echo "⚠️  Simulator not booted. Booting now..."
    xcrun simctl boot "$UDID"
    echo "✅ Simulator booted"
    sleep 3
else
    echo "✅ Simulator already booted"
fi

# Check if app is installed
echo ""
echo "🔍 Checking if app is installed..."
APP_INSTALLED=$(xcrun simctl listapps "$UDID" | grep -i "taskmanager" || echo "")
if [ -z "$APP_INSTALLED" ]; then
    echo "❌ App not installed on simulator!"
    echo ""
    echo "Please install the app first:"
    echo "  1. Build the iOS app in Xcode:"
    echo "     open ../iosApp/iosApp.xcodeproj"
    echo "  2. Or install manually:"
    echo "     xcrun simctl install $UDID <path-to-app.app>"
    echo ""
    exit 1
else
    echo "✅ App is installed"
fi

# Check if Appium is running
echo ""
echo "🔍 Checking Appium server..."
if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
    echo "❌ Appium server is not running!"
    echo ""
    echo "Please start Appium first:"
    echo "  appium"
    echo ""
    exit 1
else
    echo "✅ Appium server is running"
fi

echo ""
echo "🚀 Running performance test..."
echo ""

cd "$(dirname "$0")/.."

# Run the test with system properties
PLATFORM=IOS ./gradlew :appium:test \
  --tests PerformanceMetricsTest \
  -Dudid="$UDID" \
  -DbundleId="$BUNDLE_ID" \
  --console=plain

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✅ Test completed successfully!"
    echo ""
    echo "📊 View the metrics report:"
    LATEST_REPORT=$(ls -t build/reports/metrics/*.json 2>/dev/null | head -1)
    if [ -n "$LATEST_REPORT" ]; then
        echo "  JSON: $LATEST_REPORT"
        echo ""
        echo "Sample output:"
        cat "$LATEST_REPORT" | python3 -m json.tool | head -30
    fi
else
    echo ""
    echo "❌ Test failed with exit code: $EXIT_CODE"
    echo ""
    echo "Check the logs above for details."
fi

exit $EXIT_CODE

