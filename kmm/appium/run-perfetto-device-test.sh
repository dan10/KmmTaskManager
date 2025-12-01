#!/bin/bash

# Perfetto Device Test Runner
# Tests the complete Perfetto integration on a real Android device

set -e

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                                                                  ║"
echo "║   🚀 Perfetto Device Test                                        ║"
echo "║                                                                  ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

# Configuration
DEVICE_NAME="${DEVICE_NAME:-197cc4507d7b}"
PACKAGE_NAME="${PACKAGE_NAME:-com.danioliveira.taskmanager}"
APK_PATH="${APK_PATH:-../composeApp/build/outputs/apk/debug/composeApp-debug.apk}"

# Add trace_processor_shell to PATH
export PATH="$HOME/bin:$PATH"

echo "📋 Configuration:"
echo "   Device:  $DEVICE_NAME"
echo "   Package: $PACKAGE_NAME"
echo "   APK:     $APK_PATH"
echo ""

# Check prerequisites
echo "🔍 Checking prerequisites..."
echo ""

# 1. Check trace_processor_shell
if command -v trace_processor_shell &> /dev/null; then
    VERSION=$(trace_processor_shell --version 2>&1 | head -1)
    echo "   ✅ trace_processor_shell: $VERSION"
else
    echo "   ❌ trace_processor_shell not found!"
    echo ""
    echo "   Install it by running:"
    echo "   ./install-trace-processor.sh"
    echo ""
    exit 1
fi

# 2. Check ADB
ADB_PATH="$HOME/Library/Android/sdk/platform-tools/adb"
if [ ! -f "$ADB_PATH" ]; then
    echo "   ❌ ADB not found at: $ADB_PATH"
    exit 1
fi
echo "   ✅ ADB: $($ADB_PATH version | head -1)"

# 3. Check device
DEVICES=$($ADB_PATH devices | grep -v "List of devices" | grep "device$" | wc -l | xargs)
if [ "$DEVICES" -eq "0" ]; then
    echo "   ❌ No Android device connected!"
    echo ""
    echo "   Connect a device and run: adb devices"
    echo ""
    exit 1
fi
echo "   ✅ Device connected: $DEVICE_NAME"

# 4. Check Appium server
if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
    echo "   ❌ Appium server not running!"
    echo ""
    echo "   Start it by running: appium"
    echo ""
    exit 1
fi
echo "   ✅ Appium server running"

# 5. Check APK
if [ ! -f "$APK_PATH" ]; then
    echo "   ⚠️  APK not found at: $APK_PATH"
    echo "   Attempting to build..."
    echo ""
    cd .. && ./gradlew :composeApp:assembleDebug
    cd appium
fi
echo "   ✅ APK available"

echo ""
echo "======================================================================"
echo "🚀 Running Perfetto Device Test..."
echo "======================================================================"
echo ""

# Run the test with 5 minute timeout (Gradle startup + compilation + test)
cd .. && timeout 300 ./gradlew :appium:test \
    --tests "PerfettoDeviceTest.testLoginWithPerfetto" \
    -DdeviceName="$DEVICE_NAME" \
    -Dpackage="$PACKAGE_NAME" \
    -Dapk="$APK_PATH" \
    --no-configuration-cache

TEST_EXIT_CODE=$?
if [ $TEST_EXIT_CODE -eq 124 ]; then
    echo ""
    echo "⚠️  Test timed out after 5 minutes!"
    exit 1
fi

echo ""
echo "======================================================================"
echo "✅ Test Complete!"
echo "======================================================================"
echo ""
echo "📂 Results available at:"
echo "   build/reports/perfetto-device-test/"
echo ""
echo "📊 View the results:"
echo "   cat build/reports/perfetto-device-test/performance_result.md"
echo ""
echo "🔍 Analyze the trace:"
echo "   1. Open: https://ui.perfetto.dev/"
echo "   2. Upload: build/traces/perfetto/*.pftrace"
echo ""

