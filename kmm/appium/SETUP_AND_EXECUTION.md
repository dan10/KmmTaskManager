# Setup and Execution Guide

Complete guide for setting up the Appium performance testing environment and running tests.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Device Setup](#device-setup)
- [Running Tests](#running-tests)
- [Shell Scripts Reference](#shell-scripts-reference)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

### Required Software

#### 1. **Java Development Kit (JDK)**
- Version: JDK 17 or higher
- Check: `java -version`

#### 2. **Node.js and npm**
- Version: Node.js 16+ recommended
- Check: `node --version && npm --version`

#### 3. **Appium Server**
- Version: 2.x recommended
- Install: `npm install -g appium`
- Check: `appium --version`

#### 4. **Android Development Tools** (for Android testing)
- Android SDK Platform Tools (includes `adb`)
- Set `ANDROID_HOME` environment variable
- Check: `adb version`

#### 5. **Xcode and Command Line Tools** (for iOS testing)
- Version: Xcode 14+ recommended
- Install Command Line Tools: `xcode-select --install`
- Check: `xcodebuild -version`

#### 6. **Perfetto TraceProcessor** (for Android)
- Required for metrics extraction
- Install script provided (see below)

---

## 📦 Installation

### Step 1: Install Appium

```bash
# Install Appium globally
npm install -g appium

# Verify installation
appium --version

# Start Appium server (in a separate terminal)
appium
```

**Expected output:**
```
[Appium] Welcome to Appium v2.x.x
[Appium] Appium REST http interface listener started on 0.0.0.0:4723
```

### Step 2: Install Appium Drivers

```bash
# Install UiAutomator2 driver for Android
appium driver install uiautomator2

# Install XCUITest driver for iOS
appium driver install xcuitest

# Verify drivers
appium driver list --installed
```

### Step 3: Install Perfetto TraceProcessor (Android only)

```bash
cd /path/to/KmmTaskManager/kmm/appium
./install-trace-processor.sh
```

This script:
1. Downloads the latest `trace_processor_shell` binary
2. Installs it to `~/bin/trace_processor_shell`
3. Makes it executable
4. Adds to PATH (if needed)

**Verify installation:**
```bash
trace_processor_shell --version
```

### Step 4: Build Test Applications

#### Android (KMM Compose)
```bash
cd /path/to/KmmTaskManager/kmm
./gradlew :composeApp:assembleDebug

# For profile builds (with R8 optimizations)
./gradlew :composeApp:assembleProfile
```

**Output:** `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

#### iOS (KMM Compose)
```bash
cd /path/to/KmmTaskManager/kmm
./rebuild-ios.sh

# Or build manually in Xcode
open iosApp/iosApp.xcodeproj
# Select Release or Profile scheme
# Product → Build
```

**Output:** `.app` bundle in `DerivedData`

#### Flutter (Android)
```bash
cd /path/to/KmmTaskManager/task_manager_dart/app
flutter build apk --profile
```

#### Flutter (iOS)
```bash
cd /path/to/KmmTaskManager/task_manager_dart/app
flutter build ios --profile
```

---

## 📱 Device Setup

### Android Device/Emulator

#### Option 1: Physical Device

1. **Enable Developer Options**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging**
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

3. **Connect Device**
   ```bash
   # Connect via USB
   adb devices
   ```

   **Expected output:**
   ```
   List of devices attached
   ABC123456789    device
   ```

4. **Enable Performance Profiling**
   ```bash
   # Enable HWUI profiling (for frame timing)
   adb shell setprop debug.hwui.profile true
   
   # Verify
   adb shell getprop debug.hwui.profile
   ```

5. **Enable App-Level Tracing (Optional)**
   
   **Current Implementation:** This app uses `android.os.Trace` directly for tracing, which writes to the native atrace buffer. The `atrace_apps` configuration in Perfetto automatically captures these events.
   
   **If using AndroidX Tracing library:**
   If your app uses the `androidx.tracing:tracing` library instead of `android.os.Trace`, you must enable tracing via broadcast:
   
   ```bash
   # Set your app package name
   export package=com.danioliveira.taskmanager
   
   # Enable tracing broadcast
   adb shell am broadcast \
     -a androidx.tracing.perfetto.action.ENABLE_TRACING \
     $package/androidx.tracing.perfetto.TracingReceiver
   ```
   
   **Important Notes:**
   - The broadcast is only needed for apps using `androidx.tracing.Trace`
   - Apps using `android.os.Trace` directly (like this one) don't need the broadcast
   - The broadcast registers AndroidX tracing hooks with the Perfetto daemon
   - You can verify your package name with: `adb shell pm list packages | grep taskmanager`
   
   **Known Limitation:**
   On some devices (especially Android 15), app-level async trace events (`beginAsyncSection`/`endAsyncSection`) may not be captured reliably by Perfetto's `atrace_apps` filter. This is a known issue with Perfetto on newer Android versions. If you experience this, consider using systrace instead for app-level tracing.

#### Option 2: Android Emulator

1. **Create Emulator** (Android Studio)
   - Tools → Device Manager → Create Device
   - Choose device: Pixel 5 or newer
   - System image: Android 12+ (API 31+) recommended
   - Enable hardware acceleration

2. **Start Emulator**
   ```bash
   # List available emulators
   emulator -list-avds
   
   # Start specific emulator
   emulator -avd Pixel_5_API_33 &
   
   # Verify
   adb devices
   ```

3. **Configure Emulator**
   ```bash
   # Enable profiling
   adb shell setprop debug.hwui.profile true
   ```

### iOS Device/Simulator

#### Option 1: Physical Device

1. **Register Device** in Apple Developer Portal
2. **Trust Computer** on device when prompted
3. **Verify Connection**
   ```bash
   # List connected devices
   xcrun xctrace list devices
   ```

4. **Install App**
   ```bash
   # Build and install via Xcode
   # Or use ios-deploy
   ios-deploy --bundle path/to/App.app
   ```

#### Option 2: iOS Simulator

1. **List Simulators**
   ```bash
   xcrun simctl list devices
   ```

2. **Boot Simulator**
   ```bash
   # Boot specific simulator
   xcrun simctl boot "iPhone 14 Pro"
   
   # Or open Simulator app
   open -a Simulator
   ```

3. **Install App**
   ```bash
   xcrun simctl install booted path/to/App.app
   ```

---

## 🚀 Running Tests

### Quick Start

#### Android Perfetto Test (Recommended)
```bash
cd /path/to/KmmTaskManager/kmm/appium
./run-perfetto-device-test.sh
```

This runs a complete performance test with:
- CPU utilization tracking
- Memory usage monitoring
- FPS measurement (per-screen)
- Jank detection
- Automated report generation

#### iOS Instruments Test
```bash
cd /path/to/KmmTaskManager/kmm/appium
./run-ios-test.sh
```

### Detailed Test Execution

#### Using Gradle Directly

**Android:**
```bash
cd /path/to/KmmTaskManager/kmm

# Run specific test
./gradlew :appium:test --tests PerfettoDeviceTest.testLoginWithPerfetto

# With custom parameters
./gradlew :appium:test \
  --tests PerfettoDeviceTest \
  -DdeviceName="ABC123456789" \
  -Dpackage="com.danioliveira.taskmanager" \
  -Dapk="../composeApp/build/outputs/apk/debug/composeApp-debug.apk"
```

**iOS:**
```bash
./gradlew :appium:test --tests IosInstrumentsTest.testLoginWithInstruments
```

#### Test Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `deviceName` | Device serial or UDID | Auto-detected |
| `package` | App package name | `com.danioliveira.taskmanager` |
| `apk` | Path to APK file | `../composeApp/build/outputs/apk/debug/composeApp-debug.apk` |
| `app` | Path to iOS .app bundle | Auto-detected |
| `platform` | `ANDROID` or `IOS` | Auto-detected |

### Test Output

After test completion, results are available in:

```
kmm/appium/build/
├── reports/
│   └── perfetto-device-test/
│       ├── performance_result.json    # Machine-readable
│       ├── performance_result.md      # Human-readable
│       ├── screen_metrics.csv         # Per-screen data
│       └── screen_metrics.md          # Per-screen report
└── traces/
    └── perfetto/
        └── login_flow_*.pftrace       # Raw trace file
```

#### Viewing Results

**Markdown Report:**
```bash
cat build/reports/perfetto-device-test/performance_result.md
```

**CSV Data:**
```bash
# Open in Excel/Numbers
open build/reports/perfetto-device-test/screen_metrics.csv

# Or view in terminal
column -t -s, build/reports/perfetto-device-test/screen_metrics.csv
```

**JSON Data:**
```bash
# Pretty print
jq . build/reports/perfetto-device-test/performance_result.json

# Extract specific metric
jq '.screenMetrics[] | select(.screenName=="LoginScreen") | .fpsAvg' \
   build/reports/perfetto-device-test/performance_result.json
```

**Perfetto UI (Advanced):**
1. Open https://ui.perfetto.dev/
2. Click "Open trace file"
3. Select `build/traces/perfetto/*.pftrace`
4. Explore detailed timeline and metrics

---

## 📜 Shell Scripts Reference

### Android Scripts

#### `run-perfetto-device-test.sh`
**Purpose:** Main test runner for Android with Perfetto metrics

**Usage:**
```bash
./run-perfetto-device-test.sh

# With custom device
DEVICE_NAME=ABC123 ./run-perfetto-device-test.sh

# With custom package
PACKAGE_NAME=com.example.app ./run-perfetto-device-test.sh
```

**What it does:**
1. Checks prerequisites (adb, Appium, trace_processor)
2. Verifies device connection
3. Builds APK if needed
4. Runs PerfettoDeviceTest
5. Generates reports

**Output:** `build/reports/perfetto-device-test/`

---

#### `run-android-systrace-test.sh`
**Purpose:** Legacy systrace test (use Perfetto instead)

**Usage:**
```bash
./run-android-systrace-test.sh
```

**Note:** Systrace is deprecated in favor of Perfetto

---

#### `run-login-systrace-test.sh`
**Purpose:** Specific login flow test with systrace

**Usage:**
```bash
./run-login-systrace-test.sh
```

**Credentials:** `daniel@test.com` / `12345678`

---

#### `install-trace-processor.sh`
**Purpose:** Downloads and installs Perfetto TraceProcessor

**Usage:**
```bash
./install-trace-processor.sh
```

**What it does:**
1. Detects OS (macOS/Linux)
2. Downloads appropriate binary
3. Installs to `~/bin/trace_processor_shell`
4. Makes executable
5. Verifies installation

---

### iOS Scripts

#### `run-ios-test.sh`
**Purpose:** Main test runner for iOS with Instruments

**Usage:**
```bash
./run-ios-test.sh

# With custom device
DEVICE_NAME="iPhone 14 Pro" ./run-ios-test.sh
```

**What it does:**
1. Checks Xcode and xctrace
2. Verifies simulator/device
3. Runs IosInstrumentsTest
4. Collects Instruments traces
5. Generates reports

**Output:** `build/reports/ios-test/`

---

#### `run-ios-cpugpu-test.sh`
**Purpose:** iOS test with GPU profiling enabled

**Usage:**
```bash
./run-ios-cpugpu-test.sh
```

**What it does:**
- Same as `run-ios-test.sh` but includes GPU metrics
- Uses custom Instruments template
- Captures Metal GPU counters

**Note:** Requires physical device for GPU profiling

---

#### `run_ios_metrics_test.sh`
**Purpose:** Alternative iOS metrics collection

**Usage:**
```bash
./run_ios_metrics_test.sh
```

---

### General Scripts

#### `run-performance-test.sh`
**Purpose:** Generic performance test runner

**Usage:**
```bash
./run-performance-test.sh
```

**What it does:**
- Detects platform (Android/iOS)
- Routes to appropriate test
- Collects basic metrics

---

### Server/Load Testing Scripts (Parent Directory)

#### `setup-grafana-dashboard.sh`
**Purpose:** Sets up Grafana for server monitoring

**Location:** `kmm/setup-grafana-dashboard.sh`

**Usage:**
```bash
cd /path/to/KmmTaskManager/kmm
./setup-grafana-dashboard.sh
```

---

#### `run-load-tests.sh`
**Purpose:** Runs Gatling load tests against server

**Location:** `kmm/run-load-tests.sh`

**Usage:**
```bash
cd /path/to/KmmTaskManager/kmm
./run-load-tests.sh
```

---

#### `run-stress-tests.sh`
**Purpose:** Runs stress tests against server

**Location:** `kmm/run-stress-tests.sh`

**Usage:**
```bash
cd /path/to/KmmTaskManager/kmm
./run-stress-tests.sh
```

---

#### `rebuild-ios.sh`
**Purpose:** Rebuilds iOS app with Xcode

**Location:** `kmm/rebuild-ios.sh`

**Usage:**
```bash
cd /path/to/KmmTaskManager/kmm
./rebuild-ios.sh
```

---

## 🔍 Troubleshooting

### Common Issues

#### 1. Appium Server Not Running

**Error:**
```
Connection refused: localhost:4723
```

**Solution:**
```bash
# Start Appium in background
appium > /tmp/appium.log 2>&1 &

# Or in separate terminal
appium
```

---

#### 2. ADB Not Found

**Error:**
```
adb: command not found
```

**Solution:**
```bash
# Add Android SDK to PATH
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Or install platform-tools
brew install android-platform-tools
```

---

#### 3. Device Not Authorized

**Error:**
```
device unauthorized
```

**Solution:**
1. Disconnect and reconnect USB cable
2. Check device screen for authorization prompt
3. Tap "Allow" on device
4. Run: `adb devices`

---

#### 4. TraceProcessor Not Found

**Error:**
```
trace_processor_shell: command not found
```

**Solution:**
```bash
# Install trace processor
./install-trace-processor.sh

# Verify installation
which trace_processor_shell
trace_processor_shell --version
```

---

#### 5. No Frames Captured (FPS = 0)

**Possible causes:**
- HWUI profiling not enabled
- `view` atrace category not included
- Android version < 12 (needs fallback)

**Solution:**
```bash
# Enable HWUI profiling
adb shell setprop debug.hwui.profile true

# Verify Perfetto config includes:
# - atrace_categories: "view"
# - atrace_categories: "gfx"
# - android.surfaceflinger.frametimeline (Android 12+)

# Check trace for Choreographer events
trace_processor_shell trace.pftrace \
  -q "SELECT name FROM slice WHERE name LIKE 'Choreographer%' LIMIT 10"
```

---

#### 6. Gradle Test Hanging

**Possible causes:**
- TraceProcessor not shutting down
- Appium driver not closed
- Perfetto daemon stuck

**Solution:**
```bash
# Kill stuck processes
pkill -f trace_processor
pkill -f appium

# Restart Appium
appium > /tmp/appium.log 2>&1 &

# Run test with timeout
timeout 300 ./gradlew :appium:test --tests PerfettoDeviceTest
```

---

#### 7. iOS Simulator Not Found

**Error:**
```
No simulator found with name: iPhone 14 Pro
```

**Solution:**
```bash
# List available simulators
xcrun simctl list devices

# Boot correct simulator
xcrun simctl boot "iPhone 14 Pro"

# Or use UDID
xcrun simctl boot 12345678-1234-1234-1234-123456789ABC
```

---

#### 8. Xcode Command Line Tools Missing

**Error:**
```
xcrun: error: unable to find utility "xctrace"
```

**Solution:**
```bash
# Install Command Line Tools
xcode-select --install

# Or set active Xcode
sudo xcode-select --switch /Applications/Xcode.app

# Verify
xcodebuild -version
xcrun xctrace version
```

---

#### 9. Permission Denied on Shell Scripts

**Error:**
```
Permission denied: ./run-perfetto-device-test.sh
```

**Solution:**
```bash
# Make scripts executable
chmod +x *.sh

# Or run with bash
bash run-perfetto-device-test.sh
```

---

#### 10. Sandbox Restrictions (Gradle)

**Error:**
```
Operation not permitted
```

**Solution:**
```bash
# Run Gradle with --no-daemon
./gradlew :appium:test --no-daemon --tests PerfettoDeviceTest

# Or disable configuration cache
./gradlew :appium:test --no-configuration-cache
```

---

### Debug Mode

#### Enable Verbose Logging

**Gradle:**
```bash
./gradlew :appium:test --tests PerfettoDeviceTest --info
# or
./gradlew :appium:test --tests PerfettoDeviceTest --debug
```

**Appium:**
```bash
appium --log-level debug
```

**ADB:**
```bash
adb logcat | grep -i perfetto
```

---

### Getting Help

#### Check Logs

**Appium logs:**
```bash
tail -f /tmp/appium.log
```

**Gradle test logs:**
```bash
cat build/test-results/test/TEST-*.xml
```

**ADB logs:**
```bash
adb logcat -d > device.log
```

#### Verify Environment

```bash
# Check all prerequisites
echo "Java: $(java -version 2>&1 | head -1)"
echo "Node: $(node --version)"
echo "Appium: $(appium --version)"
echo "ADB: $(adb version | head -1)"
echo "TraceProcessor: $(trace_processor_shell --version 2>&1 | head -1)"
```

---

## 📚 Next Steps

- [Architecture Overview](ARCHITECTURE.md) - Understand the system design
- [Metrics Guide](METRICS_GUIDE.md) - Learn about collected metrics
- [Advanced Features](ADVANCED_FEATURES.md) - Explore advanced capabilities

---

## 🔗 Useful Links

- [Appium Documentation](https://appium.io/docs/en/latest/)
- [Perfetto Documentation](https://perfetto.dev/docs/)
- [AndroidX TraceProcessor](https://developer.android.com/reference/kotlin/androidx/benchmark/traceprocessor/TraceProcessor)
- [Instruments Documentation](https://developer.apple.com/documentation/xcode/instruments)
- [Perfetto UI](https://ui.perfetto.dev/)

