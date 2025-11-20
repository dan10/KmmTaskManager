#!/bin/bash

# Script to run iOS performance test with CpuAndGPU profile
# This uses Gradle to manage Instruments recording externally, avoiding Appium timeouts

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=================================================="
echo "iOS Performance Test with CpuAndGPU Profile"
echo "=================================================="
echo ""

# Check if Appium is running
if ! ps aux | grep appium | grep relaxed-security | grep -v grep > /dev/null; then
    echo -e "${RED}❌ Appium is not running with --relaxed-security${NC}"
    echo "Please start it:"
    echo "  appium --relaxed-security"
    exit 1
fi
echo -e "${GREEN}✅ Appium is running${NC}"

# Check if WebDriverAgent is running on the device
echo ""
echo "⚠️  Make sure WebDriverAgent is running on your iPhone:"
echo "   1. Open WebDriverAgent project in Xcode"
echo "   2. Select your iPhone as the target"
echo "   3. Run WebDriverAgentRunner (⌘+R)"
echo ""
read -p "Press Enter when WebDriverAgent is running..."

# Get iPhone UDID
IPHONE_UDID=$(xcrun xctrace list devices 2>&1 | grep "Daniel's iPhone" | grep -v Simulator | awk '{print $NF}' | tr -d '()')
if [ -z "$IPHONE_UDID" ]; then
    echo -e "${RED}❌ Daniel's iPhone not found${NC}"
    echo "Available devices:"
    xcrun xctrace list devices 2>&1 | grep -v Simulator
    exit 1
fi
echo -e "${GREEN}✅ Found Daniel's iPhone: $IPHONE_UDID${NC}"

# Bundle ID
BUNDLE_ID="${1:-com.danioliveira.taskmanager.KmmTaskManager}"
echo -e "${GREEN}📱 Using bundle ID: $BUNDLE_ID${NC}"

echo ""
echo "=================================================="
echo "Running Performance Test with CpuAndGPU"
echo "=================================================="
echo ""

# Run the Gradle task
./gradlew :appium:iosPerformanceTestWithCpuGpu \
    -Dudid="$IPHONE_UDID" \
    -DbundleId="$BUNDLE_ID"

echo ""
echo "=================================================="
echo "✅ Test completed!"
echo "=================================================="
echo ""
echo "Trace files are saved in:"
echo "  kmm/appium/build/ios-traces/"
echo ""
echo "To open the latest trace:"
LATEST_TRACE=$(ls -t build/ios-traces/gradle_recording_*.trace 2>/dev/null | head -1)
if [ -n "$LATEST_TRACE" ]; then
    echo "  open \"$LATEST_TRACE\""
    echo ""
    read -p "Open trace in Instruments now? [y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        open "$LATEST_TRACE"
        echo -e "${GREEN}✅ Opened in Instruments.app${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  No trace files found${NC}"
fi

