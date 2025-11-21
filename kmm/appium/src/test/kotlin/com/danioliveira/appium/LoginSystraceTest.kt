package com.danioliveira.appium

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.metrics.LegacyMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.metrics.android.PerformanceStatisticsCollector
import com.danioliveira.appium.pages.LoginPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Login flow test with systrace capture and multi-format export.
 * 
 * Captures:
 * - System-level performance via systrace
 * - Screen-level traces (LoginScreen, TasksScreen)
 * - CPU, Memory, FPS metrics
 * 
 * Exports to:
 * - JSON (structured data)
 * - CSV (spreadsheet-friendly)
 * - Markdown (human-readable report)
 * 
 * Credentials:
 * - Email: daniel@test.com
 * - Password: 12345678
 */
class LoginSystraceTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var metricsManager: MetricsManager
    private lateinit var statsCollector: PerformanceStatisticsCollector
    
    @BeforeEach
    fun setup() {
        val platform = Platform.ANDROID
        val packageName = System.getProperty("packageName") ?: "com.danioliveira.taskmanager"
        
        val config = com.danioliveira.appium.config.BenchmarkConfig(
            platform = platform,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "login-systrace",
            runs = 1,
            warmup = 0,
            deviceName = System.getProperty("deviceName") ?: "emulator-5554",
            udid = System.getProperty("udid"),
            apkPath = System.getProperty("apk"),
            ipaPath = null,
            bundleId = null,
            packageName = packageName,
            registerEveryCycle = false
        )
        
        driver = AndroidDriverFactory.create(config)
        metricsManager = LegacyMetricsManager(platform, packageName, driver = driver)
        
        // Initialize statistics collector with Flashlight FPS
        statsCollector = PerformanceStatisticsCollector(
            androidCollector = (metricsManager as? LegacyMetricsManager)?.androidCollector 
                ?: throw IllegalStateException("Android collector not available"),
            packageName = packageName
        )
        
        logger.info("✅ LoginSystraceTest setup complete")
    }
    
    @Test
    fun testLoginFlowWithSystraceExport() {
        logger.info("=== Starting Login Flow with Systrace Capture ===")
        logger.info("Credentials: daniel@test.com / 12345678")
        
        // Start systrace capture (will run until stopped)
        logger.info("Starting systrace capture...")
        metricsManager.startPerformanceRecording()
        Thread.sleep(1000)  // Let systrace initialize
        
        // Start ADB polling for real-time statistics
        logger.info("Starting ADB polling...")
        statsCollector.startPolling(intervalMs = 500)
        
        // Measure app launch (Appium overhead included)
        val appiumLaunchTime = metricsManager.measureAppLaunchTime()
        logger.info("App launch detected by Appium in ${appiumLaunchTime}ms (includes Appium overhead)")
        
        // Create login page
        val loginPage = LoginPage(driver, Platform.ANDROID, App.KMM, metricsManager)
        
        // Execute login flow with metrics tracking
        logger.info("Executing login flow...")
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("daniel@test.com")
        loginPage.enterPassword("12345678")
        loginPage.clickLogin()
        
        // Wait for navigation to complete and let app stabilize
        logger.info("Waiting for navigation...")
        Thread.sleep(5000)
        
        // Stop systrace and parse
        logger.info("Stopping systrace capture...")
        val traceMessage = metricsManager.stopPerformanceRecording("login_flow")
        logger.info("Systrace: $traceMessage")
        
        // Stop ADB polling and collect statistics
        logger.info("Stopping ADB polling...")
        val adbStats = statsCollector.stopPolling()
        logger.info("✅ ADB statistics collected")
        
        // Get all metrics
        val traceMetricsNullable = metricsManager.getTraceMetrics()
        val loginSummary = metricsManager.getPageSummary("Login")
        val totalSummary = metricsManager.getTotalSummary()
        
        // Validate trace data was captured
        require(traceMetricsNullable != null) { "❌ Systrace metrics not captured!" }
        
        // Now we can safely use traceMetrics as non-null
        val traceMetrics = traceMetricsNullable
        
        // Extract actual startup time from trace (priority order)
        val actualStartupTime: Long
        val startupSource: String
        
        when {
            // Priority 1: Use android_startup metric (most accurate)
            traceMetrics.startupTimeMs != null -> {
                actualStartupTime = traceMetrics.startupTimeMs
                startupSource = "android_startup metric"
                logger.info("✅ Actual app startup from android_startup: ${actualStartupTime}ms")
                logger.info("   Appium overhead: ${appiumLaunchTime - actualStartupTime}ms")
            }
            
            // Priority 2: Use first screen trace
            traceMetrics.screens.isNotEmpty() -> {
                val firstScreen = traceMetrics.screens.minByOrNull { it.startTimeMs }!!
                actualStartupTime = firstScreen.startTimeMs
                startupSource = "first screen trace (${firstScreen.name})"
                logger.info("✅ Actual app startup from screen trace: ${actualStartupTime}ms")
                logger.info("   First screen: ${firstScreen.name}")
                logger.info("   Appium overhead: ${appiumLaunchTime - actualStartupTime}ms")
            }
            
            // Priority 3: Fall back to Appium measurement
            else -> {
                actualStartupTime = appiumLaunchTime
                startupSource = "Appium measurement (includes overhead)"
                logger.warn("⚠️  No startup metrics found in systrace!")
                logger.warn("   - No android_startup metric")
                logger.warn("   - No screen traces (SysTrace markers not emitting)")
                logger.warn("   Falling back to Appium measurement")
                logger.info("   Using Appium launch time: ${actualStartupTime}ms")
            }
        }
        
        logger.info("=== Trace Validation ===")
        logger.info("✅ Trace file: ${traceMetrics.traceFile?.name ?: "N/A"}")
        logger.info("✅ Screen traces captured: ${traceMetrics.screenCount}")
        logger.info("✅ CPU data points: ${traceMetrics.cpuUtilization.size}")
        if (traceMetrics.cpuUtilization.isNotEmpty()) {
            logger.info("✅ CPU avg from trace: ${String.format("%.1f", traceMetrics.avgCpuPercent)}%")
            logger.info("✅ CPU peak from trace: ${String.format("%.1f", traceMetrics.peakCpuPercent)}%")
        }
        logger.info("✅ Actual startup time: ${actualStartupTime}ms")
        if (traceMetrics.startupTimeMs != null) {
            logger.info("✅ Startup from trace: ${traceMetrics.startupTimeMs}ms")
        }
        
        // Extract systrace statistics
        logger.info("=== Extracting Statistics ===")
        val systraceStats = statsCollector.extractSystraceStatistics(traceMetrics)
        val combinedStats = statsCollector.generateCombinedReport(adbStats, systraceStats)
        
        // Log statistics summary
        logger.info("=== ADB Statistics (Real-time Polling) ===")
        logger.info("Memory: min=${adbStats.memory.min}MB, max=${adbStats.memory.max}MB, avg=${String.format("%.1f", adbStats.memory.avg)}MB (${adbStats.memory.samples} samples)")
        logger.info("CPU: min=${adbStats.cpu.min}%, max=${adbStats.cpu.max}%, avg=${String.format("%.1f", adbStats.cpu.avg)}% (${adbStats.cpu.samples} samples)")
        logger.info("FPS: min=${adbStats.fps.min}, max=${adbStats.fps.max}, avg=${String.format("%.1f", adbStats.fps.avg)} (${adbStats.fps.samples} samples)")
        
        logger.info("=== Systrace Statistics (Trace Analysis) ===")
        logger.info("CPU: min=${systraceStats.cpu.min}%, max=${systraceStats.cpu.max}%, avg=${String.format("%.1f", systraceStats.cpu.avg)}% (${systraceStats.cpu.samples} samples)")
        logger.info("FPS: min=${systraceStats.fps.min}, max=${systraceStats.fps.max}, avg=${String.format("%.1f", systraceStats.fps.avg)} (${systraceStats.fps.samples} samples)")
        
        // Export to multiple formats
        val exportDir = File("build/reports/login-systrace")
        exportDir.mkdirs()
        
        logger.info("=== Exporting Results ===")
        
        // Export to JSON
        exportToJson(exportDir, traceMetrics, loginSummary, totalSummary, appiumLaunchTime, actualStartupTime)
        
        // Export to CSV
        exportToCsv(exportDir, traceMetrics, loginSummary, totalSummary, actualStartupTime)
        
        // Export to Markdown
        exportToMarkdown(exportDir, traceMetrics, loginSummary, totalSummary, appiumLaunchTime, actualStartupTime)
        
        // Export action-level metrics with deltas
        exportActionMetrics(exportDir, metricsManager.getActionMetrics())
        
        // Export statistics (min/max/avg)
        statsCollector.exportToCsv(combinedStats, File(exportDir, "performance_statistics.csv"))
        statsCollector.exportToMarkdown(combinedStats, File(exportDir, "performance_statistics.md"))
        
        logger.info("✅ All exports complete")
        logger.info("Results saved to: ${exportDir.absolutePath}")
        
        // Performance assertions
        logger.info("=== Performance Validation ===")
        require(actualStartupTime < 5000) { "❌ Startup time too slow: ${actualStartupTime}ms" }
        logger.info("✅ Startup time acceptable: ${actualStartupTime}ms < 5000ms")
        
        val loginScreen = traceMetrics.getScreenByName("LoginScreen")
        if (loginScreen != null) {
            logger.info("✅ LoginScreen duration: ${loginScreen.durationMs}ms")
            require(loginScreen.durationMs < 10000) { "❌ LoginScreen too slow: ${loginScreen.durationMs}ms" }
        }
    }
    
    private fun exportToJson(
        dir: File,
        traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics,
        loginSummary: com.danioliveira.appium.metrics.PageMetricsSummary?,
        totalSummary: com.danioliveira.appium.metrics.TotalMetricsSummary,
        appiumLaunchTime: Long,
        actualStartupTime: Long
    ) {
        val jsonFile = File(dir, "login_flow_metrics.json")
        
        val json = buildString {
            appendLine("{")
            appendLine("  \"test\": \"login_flow\",")
            appendLine("  \"timestamp\": \"${java.time.LocalDateTime.now()}\",")
            appendLine("  \"credentials\": {")
            appendLine("    \"email\": \"daniel@test.com\",")
            appendLine("    \"password\": \"********\"")
            appendLine("  },")
            
            // App launch with corrected values
            appendLine("  \"app_launch\": {")
            appendLine("    \"actual_startup_ms\": ${actualStartupTime},")
            appendLine("    \"appium_measured_ms\": ${appiumLaunchTime},")
            appendLine("    \"appium_overhead_ms\": ${appiumLaunchTime - actualStartupTime},")
            appendLine("    \"note\": \"actual_startup_ms is from systrace, appium_measured_ms includes Appium overhead\"")
            appendLine("  },")
            
            // Screen traces
            appendLine("  \"screen_traces\": [")
            traceMetrics.screens.forEachIndexed { index, screen ->
                appendLine("    {")
                appendLine("      \"name\": \"${screen.name}\",")
                appendLine("      \"start_ms\": ${screen.startTimeMs},")
                appendLine("      \"end_ms\": ${screen.endTimeMs},")
                appendLine("      \"duration_ms\": ${screen.durationMs}")
                append("    }")
                if (index < (traceMetrics.screens.size - 1)) appendLine(",")
                else appendLine()
            }
            appendLine("  ],")
            
            // Login page metrics
            appendLine("  \"login_page\": {")
            appendLine("    \"action_count\": ${loginSummary?.actionCount ?: 0},")
            appendLine("    \"total_duration_ms\": ${loginSummary?.totalDurationMs ?: 0},")
            appendLine("    \"avg_duration_ms\": ${String.format("%.2f", loginSummary?.avgDurationMs ?: 0.0)},")
            appendLine("    \"avg_memory_mb\": ${String.format("%.2f", loginSummary?.avgMemoryMb ?: 0.0)},")
            appendLine("    \"peak_memory_mb\": ${loginSummary?.peakMemoryMb ?: 0},")
            appendLine("    \"avg_cpu_percent\": ${String.format("%.2f", loginSummary?.avgCpuPercent ?: 0.0)},")
            appendLine("    \"peak_cpu_percent\": ${String.format("%.2f", loginSummary?.peakCpuPercent ?: 0.0)},")
            appendLine("    \"avg_fps\": ${String.format("%.2f", loginSummary?.avgFps ?: 0.0)},")
            appendLine("    \"avg_frame_time_ms\": ${String.format("%.2f", loginSummary?.avgFrameTimeMs ?: 0.0)},")
            appendLine("    \"avg_jank_percent\": ${String.format("%.2f", loginSummary?.avgJankPercentage ?: 0.0)}")
            appendLine("  },")
            
            // Total summary
            appendLine("  \"total_summary\": {")
            appendLine("    \"platform\": \"${totalSummary.platform}\",")
            appendLine("    \"total_actions\": ${totalSummary.totalActions},")
            appendLine("    \"avg_memory_mb\": ${String.format("%.2f", totalSummary.avgMemoryMb)},")
            appendLine("    \"peak_memory_mb\": ${totalSummary.peakMemoryMb},")
            appendLine("    \"avg_cpu_percent\": ${String.format("%.2f", totalSummary.avgCpuPercent)},")
            appendLine("    \"peak_cpu_percent\": ${String.format("%.2f", totalSummary.peakCpuPercent)},")
            appendLine("    \"avg_fps\": ${String.format("%.2f", totalSummary.avgFps)}")
            appendLine("  },")
            
            // Trace file info
            appendLine("  \"trace_file\": {")
            if (traceMetrics.traceFile != null) {
                appendLine("    \"path\": \"${traceMetrics.traceFile.absolutePath}\",")
                appendLine("    \"size_mb\": ${String.format("%.2f", traceMetrics.traceFile.length() / (1024.0 * 1024.0))},")
            } else {
                appendLine("    \"path\": null,")
                appendLine("    \"size_mb\": 0,")
            }
            appendLine("    \"screen_count\": ${traceMetrics.screenCount}")
            appendLine("  }")
            
            appendLine("}")
        }
        
        jsonFile.writeText(json)
        logger.info("✅ JSON exported: ${jsonFile.name}")
    }
    
    private fun exportToCsv(
        dir: File,
        traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics,
        loginSummary: com.danioliveira.appium.metrics.PageMetricsSummary?,
        totalSummary: com.danioliveira.appium.metrics.TotalMetricsSummary,
        actualStartupTime: Long
    ) {
        // Screen traces CSV
        val screensFile = File(dir, "screen_traces.csv")
        val screensCsv = buildString {
            appendLine("Screen Name,Start (ms),End (ms),Duration (ms)")
            traceMetrics.screens.forEach { screen ->
                appendLine("${screen.name},${screen.startTimeMs},${screen.endTimeMs},${screen.durationMs}")
            }
        }
        screensFile.writeText(screensCsv)
        logger.info("✅ CSV exported: ${screensFile.name}")
        
        // Summary metrics CSV
        val summaryFile = File(dir, "summary_metrics.csv")
        val summaryCsv = buildString {
            appendLine("Metric,Value,Unit")
            appendLine("Actual App Startup Time (from trace),${actualStartupTime},ms")
            appendLine("Appium Measured Launch Time,${totalSummary.appLaunchTimeMs},ms")
            appendLine("Login Actions,${loginSummary?.actionCount ?: 0},count")
            appendLine("Login Duration,${String.format("%.2f", loginSummary?.avgDurationMs ?: 0.0)},ms")
            appendLine("Avg Memory,${String.format("%.2f", loginSummary?.avgMemoryMb ?: 0.0)},MB")
            appendLine("Peak Memory,${loginSummary?.peakMemoryMb ?: 0},MB")
            appendLine("Avg CPU,${String.format("%.2f", loginSummary?.avgCpuPercent ?: 0.0)},%")
            appendLine("Peak CPU,${String.format("%.2f", loginSummary?.peakCpuPercent ?: 0.0)},%")
            appendLine("Avg FPS,${String.format("%.2f", loginSummary?.avgFps ?: 0.0)},fps")
            appendLine("Avg Frame Time,${String.format("%.2f", loginSummary?.avgFrameTimeMs ?: 0.0)},ms")
            appendLine("Jank Percentage,${String.format("%.2f", loginSummary?.avgJankPercentage ?: 0.0)},%")
        }
        summaryFile.writeText(summaryCsv)
        logger.info("✅ CSV exported: ${summaryFile.name}")
    }
    
    private fun exportToMarkdown(
        dir: File,
        traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics,
        loginSummary: com.danioliveira.appium.metrics.PageMetricsSummary?,
        totalSummary: com.danioliveira.appium.metrics.TotalMetricsSummary,
        appiumLaunchTime: Long,
        actualStartupTime: Long
    ) {
        val mdFile = File(dir, "login_flow_report.md")
        
        val markdown = buildString {
            appendLine("# Login Flow Performance Report")
            appendLine()
            appendLine("**Test Date**: ${java.time.LocalDateTime.now()}")
            appendLine("**Platform**: ${totalSummary.platform}")
            appendLine("**Credentials**: daniel@test.com")
            appendLine()
            
            appendLine("## App Startup Analysis")
            appendLine()
            appendLine("| Metric | Value | Notes |")
            appendLine("|--------|-------|-------|")
            appendLine("| **Actual App Startup** | **${actualStartupTime}ms** | From systrace (accurate) |")
            appendLine("| Appium Measured Launch | ${appiumLaunchTime}ms | Includes Appium overhead |")
            appendLine("| Appium Overhead | ${appiumLaunchTime - actualStartupTime}ms | Difference between measurements |")
            appendLine()
            appendLine("> ℹ️ **Note**: The actual startup time (${actualStartupTime}ms) is extracted from the systrace and represents the true app performance. The Appium measurement includes automation framework overhead.")
            appendLine()
            
            appendLine("## Summary")
            appendLine()
            appendLine("| Metric | Value |")
            appendLine("|--------|-------|")
            appendLine("| Total Actions | ${totalSummary.totalActions} |")
            appendLine("| Avg Memory | ${String.format("%.1f", totalSummary.avgMemoryMb)}MB |")
            appendLine("| Peak Memory | ${totalSummary.peakMemoryMb}MB |")
            appendLine("| Avg CPU | ${String.format("%.1f", totalSummary.avgCpuPercent)}% |")
            appendLine("| Peak CPU | ${String.format("%.1f", totalSummary.peakCpuPercent)}% |")
            appendLine("| Avg FPS | ${String.format("%.1f", totalSummary.avgFps)} |")
            appendLine()
            
            appendLine("## Screen Traces")
            appendLine()
            if (traceMetrics.screens.isNotEmpty()) {
                appendLine("| Screen | Start (ms) | End (ms) | Duration (ms) |")
                appendLine("|--------|-----------|----------|---------------|")
                traceMetrics.screens.forEach { screen ->
                    appendLine("| ${screen.name} | ${screen.startTimeMs} | ${screen.endTimeMs} | ${screen.durationMs} |")
                }
                appendLine()
                appendLine("**Total Screens**: ${traceMetrics.screenCount}")
                appendLine("**Avg Screen Duration**: ${String.format("%.1f", traceMetrics.avgScreenDurationMs)}ms")
            } else {
                appendLine("No screen traces captured.")
            }
            appendLine()
            
            appendLine("## Login Page Metrics")
            appendLine()
            if (loginSummary != null) {
                appendLine("| Metric | Value |")
                appendLine("|--------|-------|")
                appendLine("| Actions Performed | ${loginSummary.actionCount} |")
                appendLine("| Total Duration | ${loginSummary.totalDurationMs}ms |")
                appendLine("| Avg Action Duration | ${String.format("%.1f", loginSummary.avgDurationMs)}ms |")
                appendLine("| Avg Memory | ${String.format("%.1f", loginSummary.avgMemoryMb)}MB |")
                appendLine("| Peak Memory | ${loginSummary.peakMemoryMb}MB |")
                appendLine("| Avg CPU | ${String.format("%.1f", loginSummary.avgCpuPercent)}% |")
                appendLine("| Peak CPU | ${String.format("%.1f", loginSummary.peakCpuPercent)}% |")
                appendLine("| Avg FPS | ${String.format("%.1f", loginSummary.avgFps)} |")
                appendLine("| Avg Frame Time | ${String.format("%.2f", loginSummary.avgFrameTimeMs)}ms |")
                appendLine("| Jank Percentage | ${String.format("%.1f", loginSummary.avgJankPercentage)}% |")
                appendLine()
                
                appendLine("### Actions Detail")
                appendLine()
                appendLine("| Action | Duration (ms) | Memory (MB) | CPU (%) | FPS |")
                appendLine("|--------|---------------|-------------|---------|-----|")
                loginSummary.actions.forEach { action ->
                    appendLine("| ${action.actionName} | ${action.durationMs} | ${action.memoryMb} | ${String.format("%.1f", action.cpuPercent)} | ${String.format("%.1f", action.fps)} |")
                }
            } else {
                appendLine("No login page metrics available.")
            }
            appendLine()
            
            appendLine("## Trace File")
            appendLine()
            if (traceMetrics.traceFile != null) {
                appendLine("- **Path**: `${traceMetrics.traceFile.absolutePath}`")
                appendLine("- **Size**: ${String.format("%.2f", traceMetrics.traceFile.length() / (1024.0 * 1024.0))}MB")
                appendLine("- **View in Perfetto**: https://ui.perfetto.dev")
                appendLine()
                appendLine("To view the trace:")
                appendLine("1. Open https://ui.perfetto.dev")
                appendLine("2. Click 'Open trace file'")
                appendLine("3. Select: `${traceMetrics.traceFile.name}`")
                appendLine("4. Press `/` and search for `act:` to find screen traces")
            } else {
                appendLine("No trace file available.")
            }
            appendLine()
            
            appendLine("## Analysis")
            appendLine()
            
            // Screen-specific analysis
            val loginScreen = traceMetrics.getScreenByName("LoginScreen")
            val tasksScreen = traceMetrics.getScreenByName("TasksScreen")
            
            if (loginScreen != null) {
                appendLine("### LoginScreen")
                appendLine("- Duration: ${loginScreen.durationMs}ms")
                appendLine("- The login screen was active for ${String.format("%.2f", loginScreen.durationMs / 1000.0)} seconds")
                appendLine()
            }
            
            if (tasksScreen != null) {
                appendLine("### TasksScreen")
                appendLine("- Duration: ${tasksScreen.durationMs}ms")
                appendLine("- Navigation to tasks screen took ${String.format("%.2f", tasksScreen.durationMs / 1000.0)} seconds")
                appendLine()
            }
            
            // Performance assessment
            appendLine("### Performance Assessment")
            appendLine()
            val avgFps = loginSummary?.avgFps ?: 0.0
            val jankPercent = loginSummary?.avgJankPercentage ?: 0.0
            
            when {
                avgFps >= 55 && jankPercent < 5 -> {
                    appendLine("✅ **Excellent**: Smooth performance with high FPS and minimal jank")
                }
                avgFps >= 45 && jankPercent < 10 -> {
                    appendLine("✔️ **Good**: Acceptable performance with occasional frame drops")
                }
                avgFps >= 30 && jankPercent < 20 -> {
                    appendLine("⚠️ **Fair**: Noticeable performance issues, optimization recommended")
                }
                else -> {
                    appendLine("❌ **Poor**: Significant performance problems, immediate optimization needed")
                }
            }
            appendLine()
            
            appendLine("---")
            appendLine()
            appendLine("*Report generated by LoginSystraceTest*")
        }
        
        mdFile.writeText(markdown)
        logger.info("✅ Markdown exported: ${mdFile.name}")
    }
    
    @AfterEach
    fun teardown() {
        try {
            logger.info("=== Test Complete ===")
            logger.info("Reports location: build/reports/login-systrace/")
            logger.info("  - login_flow_metrics.json")
            logger.info("  - screen_traces.csv")
            logger.info("  - summary_metrics.csv")
            logger.info("  - login_flow_report.md")
            logger.info("  - action_metrics_with_deltas.csv")
            logger.info("  - performance_statistics.csv")
            logger.info("  - performance_statistics.md")
        } finally {
            driver.quit()
        }
    }
    
    private fun exportActionMetrics(dir: File, actions: List<com.danioliveira.appium.metrics.ActionMetrics>) {
        val actionsFile = File(dir, "action_metrics_with_deltas.csv")
        val actionsCsv = buildString {
            appendLine("Page,Action,Duration(ms),Memory(MB),Δ Memory(MB),CPU(%),Δ CPU(%),FPS,Avg Frame Time(ms),Jank(%)")
            actions.forEach { action ->
                // Use Locale.US to ensure decimal point (not comma) in numbers
                appendLine(
                    "${action.pageName}," +
                    "${action.actionName}," +
                    "${action.durationMs}," +
                    "${action.memoryMb}," +
                    "${action.deltaMemoryMb}," +
                    "${String.format(java.util.Locale.US, "%.1f", action.cpuPercent)}," +
                    "${String.format(java.util.Locale.US, "%.1f", action.deltaCpuPercent)}," +
                    "${String.format(java.util.Locale.US, "%.1f", action.fps)}," +
                    "${String.format(java.util.Locale.US, "%.2f", action.avgFrameTimeMs)}," +
                    "${String.format(java.util.Locale.US, "%.1f", action.jankPercentage)}"
                )
            }
        }
        actionsFile.writeText(actionsCsv)
        logger.info("✅ Action metrics CSV exported: ${actionsFile.name}")
    }
}

