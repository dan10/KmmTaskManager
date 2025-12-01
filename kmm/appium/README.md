# Appium Performance Benchmarking

This module provides automated performance benchmarking for KMM (Compose) and Flutter apps on Android and iOS using Appium.

## Setup

1. Install Appium server:
   ```bash
   npm install -g appium
   ```

2. Start Appium server:
   ```bash
   appium
   ```

3. Ensure Android emulator or iOS simulator is running.

## Building Apps

### KMM Android
```bash
cd /Users/danieloliveira/Projects/KmmTaskManager/kmm
./gradlew :composeApp:assembleProfile
```

**Note:** R8 minification is disabled for the KMM profile build due to duplicate class issues with the shared KMP module (which has both `androidTarget` and `jvm()` targets). This doesn't significantly impact benchmarking results.

### Flutter Android
```bash
cd /Users/danieloliveira/Projects/KmmTaskManager/task_manager_dart/app
flutter build apk --profile
```

**Note:** Flutter profile builds have R8 enabled (`minifyEnabled = true`) for realistic performance measurement.

### KMM iOS
Build using Xcode with Release/Profile scheme.

### Flutter iOS
```bash
cd task_manager_dart/app && flutter build ios --profile
```

## Running Benchmarks

### Android KMM
```bash
./gradlew :kmm:appium:test \
  -Pplatform=android \
  -Papp=kmm \
  -Pscenario=userJourney \
  -Pruns=15 \
  -Papk=path/to/app-profile.apk \
  -PdeviceName=Pixel_7_Pro_API_34
```

### Android Flutter
```bash
./gradlew :kmm:appium:test \
  -Pplatform=android \
  -Papp=flutter \
  -Pscenario=userJourney \
  -Pruns=15 \
  -Papk=path/to/app-profile.apk \
  -PdeviceName=Pixel_7_Pro_API_34
```

### iOS KMM
```bash
./gradlew :kmm:appium:test \
  -Pplatform=ios \
  -Papp=kmm \
  -Pscenario=userJourney \
  -Pruns=15 \
  -PbundleId=com.danioliveira.taskmanager \
  -Pudid=<simulator-udid>
```

### iOS Flutter
```bash
./gradlew :kmm:appium:test \
  -Pplatform=ios \
  -Papp=flutter \
  -Pscenario=userJourney \
  -Pruns=15 \
  -PbundleId=com.example.task_manager_app \
  -Pudid=<simulator-udid>
```

## System Properties

- `platform`: `android` or `ios` (default: `android`)
- `app`: `kmm` or `flutter` (default: `kmm`)
- `scenario`: Scenario name (default: `all`)
- `runs`: Number of benchmark runs (default: `15`)
- `warmup`: Number of warmup runs (default: `1`)
- `deviceName`: Device/emulator name
- `udid`: iOS device/simulator UDID
- `apk`: Path to Android APK
- `ipa`: Path to iOS IPA
- `bundleId`: iOS bundle ID
- `package`: Android package name
- `registerEveryCycle`: Re-register each cycle (default: `false`)

## User Journey Scenario

The user journey scenario performs the following steps (15 cycles by default):

1. Register (first cycle only, unless `registerEveryCycle=true`)
2. Login
3. Create tasks
4. View calendar
5. Create project
6. Go to project details
7. Create tasks in project
8. Go back
9. Logout
10. Login again
11. See list and scroll to end
12. Logout

## Reports

Reports are generated in `kmm/appium/build/reports/benchmarks/`.

## Element Locator Strategy

The Appium tests use **XPath with resource-id** as the primary locator strategy, as recommended by the [Android UI Automator documentation](https://developer.android.com/training/testing/other-components/ui-automator).

### For Android (Compose):
```kotlin
// Test tags are exposed as resource-id when testTagsAsResourceId = true
driver.findElement(By.xpath("//*[@resource-id='txt_email']"))
driver.findElement(By.xpath("//*[@resource-id='btn_login']"))
```

### For Android (Flutter):
```dart
// Semantics identifiers are exposed as resource-id
Semantics(identifier: "txt_email", child: TextField(...))
```

### For iOS:
```kotlin
// Test tags/identifiers are exposed as accessibilityIdentifier
driver.findElement(By.xpath("//*[@name='txt_email']"))
```

The `BasePage.findById()` method automatically tries multiple strategies:
1. XPath with `@resource-id` (Android)
2. XPath with `@content-desc` (fallback)
3. XPath with `@name` (iOS)
4. `By.id()` (last resort)

## Notes

- Ensure animations are disabled on the device for consistent results
- Close background apps before running benchmarks
- Use profile builds for realistic performance measurements
- The `<profileable android:shell="true" />` manifest element enables profiling on Android 10+
- **KMM Note:** R8 minification is disabled for KMM profile builds due to KMP shared module duplicate class issues

