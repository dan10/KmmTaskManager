# Appium Performance Testing Framework

A Flashlight-inspired performance testing framework for Kotlin/Appium with first-class support for Android (systrace) and iOS (Instruments).

## 📁 Structure

```
perf/
├── core/                       # Core types and orchestration
│   ├── TestCase.kt            # Single test case definition
│   ├── Scenario.kt            # Multi-flow scenario with iterations
│   ├── PerformanceConfig.kt   # Configuration (platform, polling, categories)
│   ├── PerformanceResult.kt   # Results with metrics and aggregations
│   └── PerformanceSession.kt  # Session orchestrator (capture + segmentation)
├── fps/                        # FPS calculation algorithms
│   ├── FlashlightGfxInfo.kt   # Android gfxinfo-based FPS (real-time)
│   ├── AtraceChoreographer.kt # Android atrace/Choreographer FPS (trace-based)
│   └── IosFps.kt              # iOS FPS from Instruments (TODO)
├── ios/                        # iOS-specific collectors
│   ├── InstrumentsSession.kt  # xctrace session manager (TODO)
│   ├── IosSignpostCollector.kt # os_signpost parser (TODO)
│   └── IosCpuCollector.kt     # CPU from Instruments (TODO)
├── export/                     # Unified exporters
│   ├── JsonExporter.kt        # JSON export
│   ├── CsvExporter.kt         # CSV export (flows, screens)
│   └── MarkdownExporter.kt    # Markdown reports
├── PerformanceMeasurement.kt  # Main entrypoints (measurePerformance, measureScenario)
└── README.md                  # This file
```

## 🚀 Quick Start

### Single Test Case

```kotlin
import com.danioliveira.appium.perf.*
import com.danioliveira.appium.perf.core.*

val result = measurePerformance(
    packageName = "com.example.app",
    testCase = TestCase(
        beforeTest = { 
            // Setup (e.g., login)
        },
        run = { 
            // Actual test actions
            navigateToProfile()
        },
        durationMs = 3000 // Optional hint
    ),
    config = PerformanceConfig(platform = Platform.ANDROID)
)

// Export all formats
result.writeAll(File("build/perf-results"))
```

### Multi-Flow Scenario (N Iterations)

```kotlin
val result = measureScenario(
    packageName = "com.example.app",
    scenario = Scenario(
        name = "User Journey",
        iterations = 3,
        flows = listOf(
            Flow(
                name = "Login",
                steps = { performLogin() },
                expectedScreens = listOf("LoginScreen", "TasksScreen"),
                durationMs = 3000
            ),
            Flow(
                name = "Create Task",
                steps = { createTask() },
                expectedScreens = listOf("CreateTaskScreen"),
                durationMs = 2000
            )
        )
    ),
    config = PerformanceConfig(platform = Platform.ANDROID)
)

result.writeAll(File("build/perf-results"))
```

## 📊 Metrics Collected

### Android (Systrace + ADB)

| Metric | Source | Method | Notes |
|--------|--------|--------|-------|
| **CPU** | Systrace | `sched_switch` events | Per-core utilization over time |
| **CPU** | ADB | `dumpsys cpuinfo` | Real-time polling (fallback) |
| **Memory** | ADB | `dumpsys meminfo` | RSS memory in MB |
| **FPS** | Systrace | Choreographer#doFrame | **Primary**: Atrace with UI-CPU adjustment |
| **FPS** | ADB | `dumpsys gfxinfo` | **Fallback**: Flashlight algorithm |
| **Screens** | Systrace | `TraceLifecycle` markers | Screen durations and transitions |

### iOS (Instruments)

| Metric | Source | Method | Notes |
|--------|--------|--------|-------|
| **CPU** | Instruments | Activity Monitor | TODO: Parse from trace export |
| **Memory** | Instruments | Activity Monitor | TODO: Parse from trace export |
| **FPS** | Instruments | CoreAnimation | TODO: Frame intervals |
| **Screens** | Instruments | os_signpost | TODO: Parse signpost events |

## 🔧 Android Setup

### Prerequisites

1. **ADB in PATH**
   ```bash
   export ANDROID_HOME="$HOME/Library/Android/sdk"
   export PATH="$ANDROID_HOME/platform-tools:$PATH"
   ```

2. **Enable HWUI Profiling** (for accurate FPS)
   ```bash
   adb shell setprop debug.hwui.profile true
   ```
   *This is automatically enforced by `PerformanceSession`.*

3. **Instrument App with SysTrace Markers**
   
   Add `TraceLifecycle` to your Compose screens:
   ```kotlin
   @Composable
   fun LoginScreen() {
       TraceLifecycle("LoginScreen")
       // ... screen content
   }
   ```

### Systrace Categories

The framework uses these categories by default:
- `sched` - CPU scheduling (**REQUIRED**)
- `freq` - CPU frequency
- `idle` - CPU idle states
- `am` - Activity Manager
- `wm` - Window Manager
- `gfx` - Graphics
- `view` - View system (**REQUIRED for Choreographer FPS**)
- `binder_driver`, `hal`, `dalvik`, `input`, `res`, `sync`, `memory`

You can customize via `PerformanceConfig.systraceCategories`.

### FPS Calculation

The framework uses **two FPS methods**:

#### 1. Atrace/Choreographer (Primary)

**Source**: Systrace `Choreographer#doFrame` events  
**Algorithm**: Flashlight's atrace FPS with UI-CPU adjustment  
**Formula**:
```
idleTime = (timeInterval - totalFrameTime) * (1 - uiCpuUsage / 100)
fps = ((frameCount + idleFrameCount) / timeInterval) * 1000
```

**Pros**:
- Most accurate (actual frame events)
- Accounts for UI thread blocking
- Single source of truth (systrace has all metrics)

**Cons**:
- Requires `view` category enabled
- Post-test analysis only (not real-time)

#### 2. Gfxinfo (Fallback)

**Source**: `dumpsys gfxinfo` frame timing  
**Algorithm**: Flashlight's gfxinfo FPS  
**Formula**:
```
renderTime = sum(frameTimes)
idleTime = max(interval - renderTime, 0)
fps = ((frameCount + idleFrameCount) / totalTime) * 1000
```

**Pros**:
- Real-time polling (works during test)
- No trace parsing needed

**Cons**:
- Less accurate (no frame-level detail)
- Doesn't account for UI thread blocking

## 🍎 iOS Setup (TODO)

### Prerequisites

1. **Xcode Command Line Tools**
   ```bash
   xcode-select --install
   ```

2. **Device in Developer Mode**
   - Settings → Privacy & Security → Developer Mode → Enable

3. **Instrument App with Signposts**
   
   Add signposts to your Swift/KMM iOS code:
   ```swift
   import os.signpost
   
   let log = OSLog(subsystem: "com.app", category: "Performance")
   os_signpost(.begin, log: log, name: "LoginScreen")
   // ... screen rendering ...
   os_signpost(.end, log: log, name: "LoginScreen")
   ```

### Instruments Templates

- **Activity Monitor**: CPU, Memory, Threads
- **Time Profiler**: Detailed CPU profiling
- **Custom**: Create template with signposts enabled

## 📤 Exports

### Single Test Case

- `performance_result.json` - Structured metrics
- `performance_result.csv` - Tabular metrics
- `performance_result.md` - Human-readable report

### Scenario (Multi-Flow)

- `scenario_summary.json` - Full scenario data
- `scenario_summary.md` - Aggregated report with 95% CI
- `flows.csv` - Per-flow, per-iteration metrics
- `screens_in_flows.csv` - Per-screen metrics within flows

## 🧪 Example Test

See [`ScenarioPerformanceTest.kt`](../ScenarioPerformanceTest.kt) for a complete example.

```kotlin
@Test
fun testMultiFlowScenario() = runBlocking {
    val scenario = Scenario(
        name = "Login to Task Creation",
        iterations = 3,
        flows = listOf(
            Flow(
                name = "Login",
                steps = { performLogin() },
                expectedScreens = listOf("LoginScreen", "TasksScreen"),
                durationMs = 3000
            ),
            Flow(
                name = "Create Task",
                steps = { createTask() },
                expectedScreens = listOf("CreateTaskScreen"),
                durationMs = 2000
            )
        )
    )
    
    val result = measureScenario(
        packageName = "com.example.app",
        scenario = scenario,
        config = PerformanceConfig(platform = Platform.ANDROID)
    )
    
    result.writeAll(File("build/reports/scenario-performance"))
}
```

## 🔍 How It Works

### Orchestration Flow

1. **Start Session** (`PerformanceSession.start()`)
   - Enforce prerequisites (HWUI profiling, etc.)
   - Start continuous systrace/xctrace capture
   - Start ADB/Instruments polling for real-time metrics

2. **Run Test Actions**
   - Execute `beforeTest` hook
   - Execute `run` or flow `steps`
   - Wait for completion (duration hint or default)

3. **Stop Session** (`PerformanceSession.stop()`)
   - Stop systrace/xctrace capture
   - Stop polling
   - Parse trace file
   - Extract metrics (CPU, Memory, FPS, screens)

4. **Segmentation** (for scenarios)
   - Match `expectedScreens` with `TraceLifecycle` markers
   - Calculate per-screen metrics from trace windows
   - Fallback to activity/window manager transitions

5. **Aggregation** (for N iterations)
   - Collect metrics per iteration
   - Calculate mean of means, min of mins, max of maxes
   - Compute 95% confidence intervals

6. **Export**
   - JSON: Structured data for programmatic access
   - CSV: Spreadsheet-friendly for analysis
   - Markdown: Human-readable reports

### Screen Segmentation

The framework segments traces by matching `expectedScreens` with app-emitted markers:

**Android**: `TraceLifecycle("ScreenName")` → `act:ScreenName` in systrace  
**iOS**: `os_signpost(.begin, name: "ScreenName")` → signpost in Instruments

If markers are missing, it falls back to system events (activity/view controller lifecycle).

## 📚 References

- [Flashlight](https://github.com/bamlab/flashlight) - Mobile performance profiler (inspiration)
- [Android Systrace](https://developer.android.com/topic/performance/tracing) - System tracing on Android
- [iOS Instruments](https://developer.apple.com/documentation/xcode/improving-your-app-s-performance) - Performance analysis on iOS
- [Perfetto](https://ui.perfetto.dev/) - Web-based trace viewer

## 🛠️ Troubleshooting

### Android

**Problem**: FPS is 0 from systrace  
**Solution**: Ensure `view` category is enabled and `Choreographer#doFrame` events are present in trace.

**Problem**: No screen traces found  
**Solution**: Add `TraceLifecycle("ScreenName")` to your Compose screens.

**Problem**: CPU data is empty  
**Solution**: Ensure `sched` category is enabled.

### iOS

**Problem**: Instruments not recording  
**Solution**: Check Developer Mode is enabled and xctrace is in PATH.

**Problem**: No signposts found  
**Solution**: Ensure `os_signpost` calls are in your app code.

## 🎯 Best Practices

1. **Use N=3 iterations minimum** for statistical significance
2. **Add TraceLifecycle to all screens** for accurate segmentation
3. **Enable HWUI profiling** for accurate Android FPS
4. **Use duration hints** to avoid manual sleeps
5. **Export all formats** for different use cases (JSON for CI, Markdown for reports)
6. **Review 95% confidence intervals** to assess measurement reliability

## 📝 TODO

- [ ] Implement iOS Instruments session management
- [ ] Parse iOS signposts from trace exports
- [ ] Add iOS CPU/Memory/FPS extraction
- [ ] Add time series downsampling for large traces
- [ ] Add comparison mode (baseline vs current)
- [ ] Add performance regression detection







