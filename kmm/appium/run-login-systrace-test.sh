#!/bin/bash

# Login Systrace Test Runner
# Runs login flow test with systrace capture and multi-format export

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}=== Login Flow Systrace Test ===${NC}"
echo "Credentials: daniel@test.com / 12345678"
echo ""

# Configuration
PACKAGE_NAME=${PACKAGE_NAME:-"com.danioliveira.taskmanager"}
DEVICE_NAME=${DEVICE_NAME:-$(adb devices | grep -v "List" | head -n 1 | awk '{print $1}')}
APK_PATH=${APK_PATH:-"../composeApp/build/outputs/apk/debug/composeApp-debug.apk"}

# Check device
if [ -z "$DEVICE_NAME" ]; then
    echo -e "${RED}❌ No Android device connected${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Device: $DEVICE_NAME${NC}"

# Check APK
if [ ! -f "$APK_PATH" ]; then
    echo -e "${YELLOW}Building APK...${NC}"
    cd ../
    ./gradlew :composeApp:assembleDebug
    cd appium
fi
echo -e "${GREEN}✅ APK: $APK_PATH${NC}"

# Check Appium
if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
    echo -e "${YELLOW}Starting Appium server...${NC}"
    appium &
    APPIUM_PID=$!
    sleep 5
else
    echo -e "${GREEN}✅ Appium server running${NC}"
    APPIUM_PID=""
fi

# Clean previous results
echo -e "${BLUE}Cleaning previous results...${NC}"
rm -rf build/reports/login-systrace
rm -rf build/traces/android/systrace_login_flow_*
mkdir -p build/reports/login-systrace
mkdir -p build/traces/android

# Run test
echo -e "${BLUE}Running login systrace test...${NC}"
echo ""

./gradlew test --tests "LoginSystraceTest" \
    -Dplatform=ANDROID \
    -DpackageName="$PACKAGE_NAME" \
    -DdeviceName="$DEVICE_NAME" \
    -Dudid="$DEVICE_NAME" \
    -Dapk="$APK_PATH" \
    --info

TEST_EXIT_CODE=$?

# Stop Appium if we started it
if [ -n "$APPIUM_PID" ]; then
    kill $APPIUM_PID 2>/dev/null || true
fi

# Show results
echo ""
echo -e "${BLUE}=== Results ===${NC}"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Test passed${NC}"
else
    echo -e "${RED}❌ Test failed (exit code: $TEST_EXIT_CODE)${NC}"
    exit $TEST_EXIT_CODE
fi

# Check exports
REPORT_DIR="build/reports/login-systrace"
if [ -d "$REPORT_DIR" ]; then
    echo ""
    echo -e "${GREEN}✅ Exports generated:${NC}"
    
    if [ -f "$REPORT_DIR/login_flow_metrics.json" ]; then
        echo "  📄 JSON: $REPORT_DIR/login_flow_metrics.json"
        echo "     Size: $(ls -lh $REPORT_DIR/login_flow_metrics.json | awk '{print $5}')"
    fi
    
    if [ -f "$REPORT_DIR/screen_traces.csv" ]; then
        echo "  📊 CSV (screens): $REPORT_DIR/screen_traces.csv"
    fi
    
    if [ -f "$REPORT_DIR/summary_metrics.csv" ]; then
        echo "  📊 CSV (summary): $REPORT_DIR/summary_metrics.csv"
    fi
    
    if [ -f "$REPORT_DIR/login_flow_report.md" ]; then
        echo "  📝 Markdown: $REPORT_DIR/login_flow_report.md"
        echo ""
        echo -e "${BLUE}Preview of Markdown report:${NC}"
        head -n 20 "$REPORT_DIR/login_flow_report.md"
        echo "..."
    fi
fi

# Check trace file
TRACE_FILE=$(find build/traces/android -name "systrace_login_flow_*.html" 2>/dev/null | head -n 1)
if [ -n "$TRACE_FILE" ]; then
    echo ""
    echo -e "${GREEN}✅ Systrace captured:${NC}"
    echo "  📈 Trace: $TRACE_FILE"
    echo "     Size: $(ls -lh $TRACE_FILE | awk '{print $5}')"
    echo ""
    echo "  View at: https://ui.perfetto.dev"
fi

echo ""
echo -e "${BLUE}=== Summary ===${NC}"
echo "Reports: $REPORT_DIR/"
echo "Trace: build/traces/android/"
echo ""
echo "Quick view:"
echo "  cat $REPORT_DIR/login_flow_report.md"
echo "  open $REPORT_DIR/login_flow_metrics.json"
echo ""

exit 0







