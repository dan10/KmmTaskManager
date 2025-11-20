#!/bin/bash

# Android Systrace Test Runner
# Runs Appium tests with systrace capture for performance analysis

set -e

# Colors for output
RED='\033[0:31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Android Systrace Test Runner ===${NC}"

# Configuration
PACKAGE_NAME=${PACKAGE_NAME:-"com.danioliveira.taskmanager"}
DEVICE_NAME=${DEVICE_NAME:-$(adb devices | grep -v "List" | head -n 1 | awk '{print $1}')}
APK_PATH=${APK_PATH:-"../composeApp/build/outputs/apk/debug/composeApp-debug.apk"}
TEST_CLASS=${TEST_CLASS:-"AndroidSystraceTest"}

# Check prerequisites
echo -e "${BLUE}Checking prerequisites...${NC}"

# Check ADB
if ! command -v adb &> /dev/null; then
    echo -e "${RED}❌ ADB not found. Please install Android SDK.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ ADB found${NC}"

# Check device connection
if [ -z "$DEVICE_NAME" ]; then
    echo -e "${RED}❌ No Android device connected${NC}"
    echo "Please connect a device via USB and enable USB debugging"
    exit 1
fi
echo -e "${GREEN}✅ Device connected: $DEVICE_NAME${NC}"

# Check API level
API_LEVEL=$(adb -s "$DEVICE_NAME" shell getprop ro.build.version.sdk | tr -d '\r')
echo "Device API level: $API_LEVEL"

if [ "$API_LEVEL" -lt 29 ]; then
    echo -e "${YELLOW}⚠️  Warning: API level < 29. Async traces may not work properly.${NC}"
    echo -e "${YELLOW}   For best results, use Android 10+ (API 29+)${NC}"
else
    echo -e "${GREEN}✅ API level $API_LEVEL supports async traces${NC}"
fi

# Check APK
if [ ! -f "$APK_PATH" ]; then
    echo -e "${YELLOW}⚠️  APK not found at: $APK_PATH${NC}"
    echo "Building APK..."
    cd ../
    ./gradlew :composeApp:assembleDebug
    cd appium
    
    if [ ! -f "$APK_PATH" ]; then
        echo -e "${RED}❌ Failed to build APK${NC}"
        exit 1
    fi
fi
echo -e "${GREEN}✅ APK found: $APK_PATH${NC}"

# Check Appium server
echo -e "${BLUE}Checking Appium server...${NC}"
if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Appium server not running${NC}"
    echo "Starting Appium server..."
    appium &
    APPIUM_PID=$!
    sleep 5
    
    if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
        echo -e "${RED}❌ Failed to start Appium server${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Appium server started (PID: $APPIUM_PID)${NC}"
else
    echo -e "${GREEN}✅ Appium server running${NC}"
    APPIUM_PID=""
fi

# Clean previous traces
echo -e "${BLUE}Cleaning previous traces...${NC}"
rm -rf build/traces/android
rm -rf build/reports/systrace
mkdir -p build/traces/android
mkdir -p build/reports/systrace

# Run test
echo -e "${BLUE}Running systrace test...${NC}"
echo "Package: $PACKAGE_NAME"
echo "Device: $DEVICE_NAME"
echo "Test: $TEST_CLASS"
echo ""

./gradlew test --tests "$TEST_CLASS" \
    -Dplatform=ANDROID \
    -DpackageName="$PACKAGE_NAME" \
    -DdeviceName="$DEVICE_NAME" \
    -Dudid="$DEVICE_NAME" \
    -Dapk="$APK_PATH" \
    --info

TEST_EXIT_CODE=$?

# Stop Appium if we started it
if [ -n "$APPIUM_PID" ]; then
    echo -e "${BLUE}Stopping Appium server...${NC}"
    kill $APPIUM_PID 2>/dev/null || true
fi

# Report results
echo ""
echo -e "${BLUE}=== Test Results ===${NC}"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Tests passed${NC}"
else
    echo -e "${RED}❌ Tests failed (exit code: $TEST_EXIT_CODE)${NC}"
fi

# Check for trace files
TRACE_COUNT=$(find build/traces/android -name "*.html" 2>/dev/null | wc -l)
if [ $TRACE_COUNT -gt 0 ]; then
    echo -e "${GREEN}✅ Found $TRACE_COUNT trace file(s)${NC}"
    echo "Trace files:"
    find build/traces/android -name "*.html" -exec ls -lh {} \;
    echo ""
    echo "View traces at: https://ui.perfetto.dev"
    echo "Or open locally: build/traces/android/"
else
    echo -e "${YELLOW}⚠️  No trace files found${NC}"
fi

# Check for reports
if [ -d "build/reports/systrace" ] && [ "$(ls -A build/reports/systrace)" ]; then
    echo -e "${GREEN}✅ Reports generated${NC}"
    echo "Reports: build/reports/systrace/"
    ls -lh build/reports/systrace/
fi

echo ""
echo -e "${BLUE}=== Summary ===${NC}"
echo "Traces: build/traces/android/"
echo "Reports: build/reports/systrace/"
echo "Logs: build/test-results/"

exit $TEST_EXIT_CODE







