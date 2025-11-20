#!/bin/bash

# iOS Performance Metrics Test Runner
# Run this script to test real metrics extraction on your iPhone

set -e

echo "🚀 iOS Performance Metrics Test"
echo "================================"
echo ""
echo "📱 Device: Daniel's iPhone (iPhone 13)"
echo "📊 Profile: Activity Monitor"
echo "🎯 Testing: Real CPU & Memory metrics extraction"
echo ""

# Check if iPhone is connected
echo "🔍 Checking device connection..."
if ! xcrun xctrace list devices 2>&1 | grep -q "00008110-000C6DA40110401E"; then
    echo "❌ iPhone not found! Please connect Daniel's iPhone"
    exit 1
fi
echo "✅ iPhone connected"
echo ""

# Check if Appium is running
echo "🔍 Checking Appium server..."
if ! ps aux | grep -v grep | grep -q "appium.*relaxed-security"; then
    echo "⚠️  Appium not running with --relaxed-security"
    echo "   Starting Appium..."
    appium --relaxed-security > /tmp/appium.log 2>&1 &
    sleep 3
    echo "✅ Appium started"
else
    echo "✅ Appium running"
fi
echo ""

# Clean up old traces
echo "🧹 Cleaning up old traces..."
rm -rf build/ios-traces
mkdir -p build/ios-traces
echo "✅ Clean!"
echo ""

# Reminder
echo "⚠️  IMPORTANT: Make sure your iPhone is UNLOCKED!"
echo ""
read -p "Press Enter when iPhone is unlocked..."
echo ""

# Run the test
echo "🧪 Running performance test..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd /Users/danieloliveira/.cursor/worktrees/KmmTaskManager/3Uiuw/kmm

PLATFORM=IOS ./gradlew :appium:test \
  --tests PerformanceMetricsTest.testLoginFlowWithMetrics \
  -Dudid=00008110-000C6DA40110401E \
  -DbundleId=com.danioliveira.taskmanager.KmmTaskManager \
  -DinstrumentsProfile="Activity Monitor" \
  --rerun-tasks \
  --console=plain

TEST_EXIT_CODE=$?

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ TEST PASSED!"
    echo ""
    
    # Show results
    echo "📊 RESULTS:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # Find latest JSON report
    LATEST_JSON=$(ls -t appium/build/reports/metrics/*.json 2>/dev/null | head -1)
    
    if [ -n "$LATEST_JSON" ]; then
        echo ""
        echo "📄 JSON Report: $LATEST_JSON"
        echo ""
        
        # Extract key metrics
        echo "🔹 CPU Metrics:"
        cat "$LATEST_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"   Average: {data.get('avgCpuPercent', 'N/A')}%\"); print(f\"   Peak: {data.get('peakCpuPercent', 'N/A')}%\")"
        
        echo ""
        echo "🔹 Memory Metrics:"
        cat "$LATEST_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"   Average: {data.get('avgMemoryMb', 'N/A')} MB\"); print(f\"   Peak: {data.get('peakMemoryMb', 'N/A')} MB\")"
        
        echo ""
        echo "🔹 Actions:"
        cat "$LATEST_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"   Total: {data.get('totalActions', 'N/A')}\"); print(f\"   Duration: {data.get('totalDurationMs', 'N/A')} ms\")"
        
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "📁 Full report: $LATEST_JSON"
        echo ""
        
        # Check if metrics are real (not placeholders)
        AVG_CPU=$(cat "$LATEST_JSON" | python3 -c "import sys, json; print(json.load(sys.stdin).get('avgCpuPercent', 0))")
        if (( $(echo "$AVG_CPU > 0 && $AVG_CPU != 15" | bc -l) )); then
            echo "🎉 SUCCESS! Got REAL metrics from trace file!"
        else
            echo "⚠️  Metrics might be placeholders. Check logs for parsing errors."
        fi
    else
        echo "⚠️  No JSON report found"
    fi
    
    echo ""
    echo "📂 Trace files: appium/build/ios-traces/"
    ls -lh appium/build/ios-traces/ 2>/dev/null || echo "   (no traces)"
    
else
    echo "❌ TEST FAILED (exit code: $TEST_EXIT_CODE)"
    echo ""
    echo "Check the error above or:"
    echo "  - Gradle test report: appium/build/reports/tests/test/index.html"
    echo "  - Appium logs: /tmp/appium.log"
fi

echo ""
echo "Done! 🎊"


