#!/bin/bash

# Performance Metrics Test Runner
# This script helps you run the performance metrics collection test

set -e

echo "🚀 Performance Metrics Test Runner"
echo "===================================="
echo ""

# Check if Appium is installed
if ! command -v appium &> /dev/null; then
    echo "❌ Appium is not installed"
    echo "   Install: npm install -g appium"
    exit 1
fi

# Check if Appium server is running
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
echo "📊 Running Performance Metrics Test..."
echo ""

# Navigate to appium directory
cd "$(dirname "$0")"

# Run the test
../gradlew test --tests PerformanceMetricsTest \
    -Dplatform=ANDROID \
    --info \
    2>&1 | tee /tmp/performance-test.log

TEST_EXIT_CODE=${PIPESTATUS[0]}

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ Test completed successfully!"
    echo ""
    echo "📁 Reports generated:"
    
    # Find and list generated reports
    REPORTS_DIR="build/reports/metrics"
    if [ -d "$REPORTS_DIR" ]; then
        echo ""
        ls -lht "$REPORTS_DIR" | head -10
        echo ""
        echo "📊 View HTML report:"
        LATEST_HTML=$(find "$REPORTS_DIR" -name "*.html" -type f -print0 | xargs -0 ls -t | head -1)
        if [ -n "$LATEST_HTML" ]; then
            echo "   $LATEST_HTML"
            echo ""
            echo "Opening report in browser..."
            open "$LATEST_HTML" || xdg-open "$LATEST_HTML" 2>/dev/null || echo "   (Manual open required)"
        fi
    else
        echo "   No reports found in $REPORTS_DIR"
    fi
else
    echo "❌ Test failed with exit code $TEST_EXIT_CODE"
    echo ""
    echo "📋 Check logs:"
    echo "   tail -100 /tmp/performance-test.log"
fi

# Cleanup Appium if we started it
if [ -n "$APPIUM_PID" ]; then
    echo ""
    echo "Stopping Appium server (PID: $APPIUM_PID)..."
    kill $APPIUM_PID 2>/dev/null || true
fi

exit $TEST_EXIT_CODE

