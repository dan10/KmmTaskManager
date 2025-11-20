#!/bin/bash

# Set Android SDK environment variables
export ANDROID_HOME=$HOME/Library/Android/sdk
export ANDROID_SDK_ROOT=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

# Verify ADB is accessible
if ! command -v adb &> /dev/null; then
    echo "Error: adb not found. Please ensure Android SDK is installed."
    exit 1
fi

# Verify device is connected
DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')
if [ "$DEVICES" -eq 0 ]; then
    echo "Error: No Android device connected. Please connect a device and enable USB debugging."
    exit 1
fi

# Check if Appium server is running
APPIUM_PID=$(ps aux | grep -i "appium" | grep -v grep | awk '{print $2}' | head -1)
if [ -n "$APPIUM_PID" ]; then
    echo "⚠️  Appium server is already running (PID: $APPIUM_PID)"
    echo "⚠️  It may not have ANDROID_HOME set. Restarting with correct environment..."
    kill $APPIUM_PID 2>/dev/null || true
    sleep 2
fi

# Start Appium server with ANDROID_HOME set (in background)
echo "🚀 Starting Appium server with ANDROID_HOME=$ANDROID_HOME..."
ANDROID_HOME="$ANDROID_HOME" ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT" \
  appium > /tmp/appium.log 2>&1 &
APPIUM_PID=$!
echo "Appium server started (PID: $APPIUM_PID)"
echo "Waiting 5 seconds for Appium to initialize..."
sleep 5

echo "Android SDK: $ANDROID_HOME"
echo "Connected devices: $DEVICES"
echo ""
echo "Starting FlowCompletionPerformanceTest..."
echo ""

# Run the test with environment variables
cd "$(dirname "$0")"
ANDROID_HOME="$ANDROID_HOME" ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT" \
  ../gradlew --no-configuration-cache test --tests FlowCompletionPerformanceTest \
  -DdeviceName=197cc4507d7b \
  -Dapk=../composeApp/build/outputs/apk/debug/composeApp-debug.apk \
  -Pandroid.home="$ANDROID_HOME"

# Capture exit code
EXIT_CODE=$?

# Optionally stop Appium server (uncomment if you want to auto-stop)
# echo ""
# echo "Stopping Appium server..."
# kill $APPIUM_PID 2>/dev/null || true

exit $EXIT_CODE


