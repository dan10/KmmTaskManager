#!/bin/bash
# iOS Real Device Test Runner - Daniel's iPhone
# UDID:

set -e

APP=${1:-flutter}
DEVICE_UDID="Use your UDID"

echo "📱 iOS Real Device Test Runner"
echo "=============================="
echo "Device: Daniel's iPhone"
echo "UDID: $DEVICE_UDID"
echo "App: $APP"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Appium is running
if ! curl -s http://127.0.0.1:4723/status > /dev/null 2>&1; then
    echo -e "${RED}❌ Appium is not running!${NC}"
    echo "Start Appium in another terminal:"
    echo "  appium"
    exit 1
fi

echo -e "${GREEN}✅ Appium is running${NC}"

# Check if device is connected
if ! xcrun xctrace list devices 2>&1 | grep -q "$DEVICE_UDID"; then
    echo -e "${RED}❌ iPhone not connected!${NC}"
    echo "Please connect your iPhone via USB"
    exit 1
fi

echo -e "${GREEN}✅ iPhone is connected${NC}"

# Check if app is installed
BUNDLE_ID="com.danieloliveira.taskManagerApp"
if ! xcrun devicectl device info apps --device "$DEVICE_UDID" 2>/dev/null | grep -q "$BUNDLE_ID"; then
    echo -e "${YELLOW}⚠️  App might not be installed${NC}"
    echo "If the test fails, install the app first:"
    echo "  cd ../task_manager_dart/app"
    echo "  flutter run --release"
fi

echo ""
echo "Running test on real device..."
echo ""

# Run the test
cd "$(dirname "$0")"
./gradlew :appium:test \
  --tests "com.danioliveira.appium.FrameworkComparisonTest.testSingleIterationIOS" \
  -Dudid="$DEVICE_UDID"

echo ""
echo -e "${GREEN}✅ Test complete!${NC}"
echo "Reports available in: build/reports/metrics/"
