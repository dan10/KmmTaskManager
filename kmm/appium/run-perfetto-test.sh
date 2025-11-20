#!/bin/bash

# Perfetto Device Test Runner
# Tests the new Perfetto metrics extraction with proper shutdown

set -e

echo "🚀 Perfetto Device Test Runner"
echo "================================"
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check if trace_processor_shell is installed
if ! command -v trace_processor_shell &> /dev/null; then
    if [ ! -f "$HOME/bin/trace_processor_shell" ]; then
        echo "❌ trace_processor_shell not found"
        echo "   Run: ./install-trace-processor.sh"
        exit 1
    fi
fi
echo "✅ trace_processor_shell found"

# Check if Appium is running
if ! nc -z localhost 4723 2>/dev/null; then
    echo "⚠️  Appium server is not running on port 4723"
    echo "   Starting Appium server in background..."
    appium > /tmp/appium.log 2>&1 &
    APPIUM_PID=$!
    echo "   Appium PID: $APPIUM_PID"
    sleep 3
    
    if ! nc -z localhost 4723 2>/dev/null; then
        echo "❌ Failed to start Appium server"
        echo "   Check logs: tail /tmp/appium.log"
        exit 1
    fi
    echo "✅ Appium server started"
else
    echo "✅ Appium server is running"
fi

# Check for Android devices
echo ""
echo "Checking for Android devices..."
ADB_DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l | xargs)

if [ "$ADB_DEVICES" -eq "0" ]; then
    echo "⚠️  No Android devices found"
    echo "   Please start an emulator or connect a device"
    echo "   Run: adb devices"
    exit 1
else
    echo "✅ Found $ADB_DEVICES Android device(s)"
    adb devices
fi

echo ""
echo "📊 Running Perfetto Device Test..."
echo ""

# Navigate to appium directory
cd "$(dirname "$0")"

# Run the test with timeout
../gradlew :appium:test --tests PerfettoDeviceTest \
    -Dplatform=ANDROID \
    --info \
    2>&1 | tee /tmp/perfetto-test.log

TEST_EXIT_CODE=${PIPESTATUS[0]}

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ Test completed successfully!"
    echo ""
    echo "📁 Results generated:"
    
    # Find and list generated reports
    REPORTS_DIR="build/reports/perfetto-device-test"
    if [ -d "$REPORTS_DIR" ]; then
        echo ""
        ls -lht "$REPORTS_DIR" | head -10
        echo ""
        echo "📊 View reports:"
        echo "   JSON: $REPORTS_DIR/performance_result.json"
        echo "   MD:   $REPORTS_DIR/performance_result.md"
    fi
    
    # List trace files
    TRACES_DIR="build/traces/perfetto"
    if [ -d "$TRACES_DIR" ]; then
        echo ""
        echo "📈 Trace files:"
        ls -lht "$TRACES_DIR" | head -5
        echo ""
        echo "💡 Upload traces to https://ui.perfetto.dev/ for analysis"
    fi
else
    echo "❌ Test failed with exit code $TEST_EXIT_CODE"
    echo ""
    echo "📋 Check logs:"
    echo "   tail -100 /tmp/perfetto-test.log"
    echo ""
    echo "Common issues:"
    echo "   1. TraceProcessor hanging: Check if shutdown() is called"
    echo "   2. No package name: Verify app is installed"
    echo "   3. Perfetto not available: Check device API level (need 28+)"
fi

# Cleanup Appium if we started it
if [ -n "$APPIUM_PID" ]; then
    echo ""
    echo "Stopping Appium server (PID: $APPIUM_PID)..."
    kill $APPIUM_PID 2>/dev/null || true
fi

exit $TEST_EXIT_CODE





