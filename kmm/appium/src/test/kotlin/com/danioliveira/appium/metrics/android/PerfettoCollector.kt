package com.danioliveira.appium.metrics.android

import com.danioliveira.appium.perf.core.PerformanceConfig
import com.danioliveira.appium.utils.AdbShell
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages Perfetto trace capture for Android performance analysis.
 * 
 * Perfetto is the modern replacement for systrace, offering:
 * - Better performance and lower overhead
 * - Richer data sources (memory, power, etc.)
 * - SQL-based querying via TraceProcessor
 * - Active development and support
 * 
 * **Data Sources:**
 * - `linux.ftrace`: Kernel-level tracing (CPU scheduling, etc.)
 * - `linux.process_stats`: Process memory and CPU statistics
 * - `track_event`: Perfetto SDK events (for apps using Perfetto SDK)
 * - `android.surfaceflinger.frame`: Frame timing data (Android 12+)
 * - `android.surfaceflinger.frametimeline`: Frame timeline events (Android 12+)
 * 
 * **App-Level Tracing:**
 * This app uses `android.os.Trace` directly, which writes to the native atrace
 * buffer. The `atrace_apps` configuration automatically captures these events.
 * 
 * If using `androidx.tracing:tracing` library, enable tracing via broadcast:
 * ```bash
 * adb shell am broadcast -a androidx.tracing.perfetto.action.ENABLE_TRACING \
 *   com.danioliveira.taskmanager/androidx.tracing.perfetto.TracingReceiver
 * ```
 * 
 * **Known Limitation:**
 * On some devices (especially Android 15), async trace events from `android.os.Trace`
 * may not be captured by Perfetto's `atrace_apps`. This is a known Perfetto issue.
 * 
 * Reference: https://perfetto.dev/docs/quickstart/android-tracing
 */
class PerfettoCollector {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val outputDir = File("build/traces/perfetto").absoluteFile.apply { mkdirs() }
    
    private var captureStartTime: Long = 0
    private var isCapturing = false
    private var currentPackageName: String? = null
    
    companion object {
        // Default data sources for performance testing
        val DEFAULT_DATA_SOURCES = listOf(
            "linux.ftrace",           // Kernel tracing (CPU scheduling, etc.)
            "linux.process_stats",    // Process memory and CPU stats
            "android.surfaceflinger.frame"  // Frame timing (Android 12+)
        )
        
        // Default ftrace events
        val DEFAULT_FTRACE_EVENTS = listOf(
            "sched/sched_switch",
            "sched/sched_wakeup",
            "power/suspend_resume",
            "sched/sched_process_exit",
            "sched/sched_process_free",
            "task/task_newtask",
            "task/task_rename"
        )
        
        // Essential atrace categories for performance metrics
        // Focused on what we actually need for CPU, FPS, and app-level tracing
        val DEFAULT_ATRACE_CATEGORIES = listOf(
            "app",      // ESSENTIAL: androidx.tracing custom traces (act:LoginScreen, act:TasksScreen)
            "sched",    // ESSENTIAL: CPU scheduling and utilization analysis
            "gfx",      // ESSENTIAL: FPS (SurfaceFlinger, VSYNC events)
            "view",     // View system interaction
            "dalvik"    // Garbage collection events (performance impact)
        )
    }
    
    /**
     * Ensure Perfetto traced services are enabled on the device.
     * Required for Perfetto to work.
     */
    fun ensureTracedServicesEnabled(): Boolean {
        return try {
            logger.info("Ensuring Perfetto traced services are enabled...")
            AdbShell.exec("shell", "setprop", "persist.traced.enable", "1")
            Thread.sleep(1000) // Give services time to start
            logger.info("✅ Perfetto services enabled")
            true
        } catch (e: Exception) {
            logger.error("Failed to enable Perfetto services: ${e.message}")
            false
        }
    }
    
    /**
     * Start Perfetto trace capture in background mode.
     * 
     * @param config Performance configuration
     * @param packageName App package name for app-level tracing
     * @return true if capture started successfully
     */
    fun startCapture(
        config: PerformanceConfig,
        packageName: String? = null
    ): Boolean {
        if (isCapturing) {
            logger.warn("Perfetto capture already in progress")
            return false
        }
        
        try {
            // Ensure services are enabled
            if (!ensureTracedServicesEnabled()) {
                return false
            }
            
            // Generate trace configuration
            val traceConfig = generateTraceConfig(config, packageName)
            
            logger.info("Starting Perfetto capture...")
            logger.debug("Trace config:\n$traceConfig")
            
            // Start perfetto in background mode using stdin for config
            // This avoids permission issues with /data/local/tmp
            // Use /data/misc/perfetto-traces/ - the only directory Perfetto can write to
            // Reference: AndroidX Benchmark PerfettoHelper.kt
            val output = AdbShell.execWithStdin(
                stdin = traceConfig,
                "shell",
                "perfetto",
                "--txt",
                "--config", "-",  // Read config from stdin
                "-o", "/data/misc/perfetto-traces/trace.pftrace",
                "--background"
            )
            
            logger.debug("Perfetto start output: $output")
            
            captureStartTime = System.currentTimeMillis()
            isCapturing = true
            currentPackageName = packageName
            
            logger.info("✅ Perfetto capture started successfully")
            logger.info("   Trace will be saved to /data/misc/perfetto-traces/trace.pftrace")
            
            // Give perfetto a moment to initialize
            Thread.sleep(500)
            
            return true
            
        } catch (e: Exception) {
            logger.error("Failed to start Perfetto capture: ${e.message}", e)
            isCapturing = false
            return false
        }
    }
    
    /**
     * Stop Perfetto capture and pull the trace file.
     * 
     * @param testName Name to include in trace filename
     * @return File containing the trace, or null if capture failed
     */
    fun stopCapture(testName: String = "trace"): File? {
        if (!isCapturing) {
            logger.warn("No Perfetto capture in progress")
            return null
        }
        
        try {
            logger.info("Stopping Perfetto capture...")
            
            // Stop perfetto (kills the background process)
            try {
                AdbShell.exec("shell", "killall", "perfetto", retries = 1)
            } catch (e: Exception) {
                logger.debug("killall perfetto returned error (expected if already stopped)")
            }
            
            // Wait for trace to be written
            Thread.sleep(1000)
            
            val captureDuration = System.currentTimeMillis() - captureStartTime
            logger.info("Capture duration: ${captureDuration}ms")
            
            // Pull trace file from device
            val timestamp = dateFormat.format(Date())
            val outputFile = File(outputDir, "perfetto_${testName}_${timestamp}.pftrace")
            
            // Ensure output directory exists
            outputFile.parentFile?.mkdirs()
            
            // Make trace file readable by ADB (required for non-root devices)
            // This is the AndroidX Benchmark approach
            try {
                AdbShell.exec("shell", "chmod", "644", "/data/misc/perfetto-traces/trace.pftrace", retries = 0)
                logger.debug("Made trace file readable")
            } catch (e: Exception) {
                logger.warn("Failed to chmod trace file (may fail on some devices): ${e.message}")
            }
            
            logger.info("Pulling trace file to ${outputFile.absolutePath}...")
            AdbShell.exec("pull", "/data/misc/perfetto-traces/trace.pftrace", outputFile.absolutePath)
            
            // Clean up trace file from device
            try {
                AdbShell.exec("shell", "rm", "/data/misc/perfetto-traces/trace.pftrace", retries = 0)
            } catch (e: Exception) {
                logger.debug("Failed to clean up trace file on device: ${e.message}")
            }
            
            if (!outputFile.exists() || outputFile.length() == 0L) {
                logger.error("Failed to pull trace file or file is empty")
                return null
            }
            
            logger.info("✅ Trace file saved: ${outputFile.absolutePath} (${outputFile.length() / 1024}KB)")
            
            isCapturing = false
            return outputFile
            
        } catch (e: Exception) {
            logger.error("Failed to stop Perfetto capture: ${e.message}", e)
            isCapturing = false
            return null
        }
    }
    
    /**
     * Parse a Perfetto trace file and extract metrics.
     * 
     * This uses the PerfettoMetricsExtractor to query the trace via SQL.
     * 
     * @param traceFile The .pftrace file to parse
     * @return TraceMetrics containing extracted performance data
     */
    fun parseTrace(traceFile: File): TraceMetrics {
        logger.info("Parsing Perfetto trace: ${traceFile.name}")
        
        val packageName = currentPackageName
        if (packageName == null) {
            logger.warn("No package name available for trace parsing")
            return TraceMetrics(
                traceFile = traceFile,
                screens = emptyList(),
                totalDurationMs = 0,
                cpuUtilization = emptyList(),
                startupTimeMs = null,
                fps = 0.0,
                frameCount = 0
            )
        }
        
        return try {
            // Use PerfettoMetricsExtractor to extract metrics via SQL
            com.danioliveira.appium.metrics.android.perfetto.PerfettoMetricsExtractor(traceFile).use { extractor ->
                // Extract metrics for the package
                extractor.extractMetrics(packageName)
            }
        } catch (e: Exception) {
            logger.error("Failed to parse Perfetto trace: ${e.message}", e)
            // Return empty metrics on error
            TraceMetrics(
                traceFile = traceFile,
                screens = emptyList(),
                totalDurationMs = 0,
                cpuUtilization = emptyList(),
                startupTimeMs = null,
                fps = 0.0,
                frameCount = 0
            )
        }
    }
    
    /**
     * Generate Perfetto trace configuration in text format.
     * 
     * Reference: https://perfetto.dev/docs/concepts/config
     */
    private fun generateTraceConfig(
        config: PerformanceConfig,
        packageName: String?
    ): String {
        val bufferSizeKb = config.systraceBufferSizeKb
        
        return buildString {
            // Duration: 0 means trace until explicitly stopped
            appendLine("duration_ms: 0")
            appendLine()
            
            // Buffer configuration
            appendLine("buffers: {")
            appendLine("    size_kb: $bufferSizeKb")
            appendLine("    fill_policy: DISCARD")
            appendLine("}")
            appendLine()
            
            // Ftrace data source (kernel tracing)
            appendLine("data_sources: {")
            appendLine("    config {")
            appendLine("        name: \"linux.ftrace\"")
            appendLine("        ftrace_config {")
            
            // Add ftrace events
            DEFAULT_FTRACE_EVENTS.forEach { event ->
                appendLine("            ftrace_events: \"$event\"")
            }
            
            // Add atrace categories
            config.systraceCategories.forEach { category ->
                appendLine("            atrace_categories: \"$category\"")
            }
            
            // Add app-specific tracing if package name provided
            if (packageName != null) {
                appendLine("            atrace_apps: \"$packageName\"")
            }
            
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            
            // CPU info data source (frequency and idle states)
            appendLine("data_sources: {")
            appendLine("    config {")
            appendLine("        name: \"linux.cpu_info\"")
            appendLine("    }")
            appendLine("}")
            appendLine()
            
            // System stats data source (CPU frequency, memory info)
            appendLine("data_sources: {")
            appendLine("    config {")
            appendLine("        name: \"linux.sys_stats\"")
            appendLine("        sys_stats_config {")
            appendLine("            cpufreq_period_ms: 1000")
            appendLine("            meminfo_period_ms: 1000")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            
            // Android power data source (battery, CPU per UID)
            // Note: This data source may not be available on all devices
            // Commenting out for now as it causes startup failures on some devices
            // if (packageName != null) {
            //     appendLine("data_sources: {")
            //     appendLine("    config {")
            //         appendLine("        name: \"android.power\"")
            //         appendLine("        android_power_config {")
            //             appendLine("            battery_poll_ms: 1000")
            //             appendLine("            collect_power_rails: true")
            //         appendLine("        }")
            //     appendLine("    }")
            //     appendLine("}")
            //     appendLine()
            // }
            
            // Process stats data source (per-process stats including memory counters)
            appendLine("data_sources: {")
            appendLine("    config {")
            appendLine("        name: \"linux.process_stats\"")
            appendLine("        process_stats_config {")
            appendLine("            scan_all_processes_on_start: true")
            appendLine("            proc_stats_poll_ms: 1000")  // Poll every 1 second for memory RSS
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            
            // Track event data source (for Jetpack/Perfetto SDK events)
            // This captures events from androidx.tracing and Perfetto SDK
            appendLine("data_sources: {")
            appendLine("    config {")
            appendLine("        name: \"track_event\"")
            appendLine("    }")
            appendLine("}")
            appendLine()
            
            // Frame timeline (Android 12+)
            val apiLevel = AdbShell.getApiLevel()
            if (apiLevel >= 31) { // Android 12 (API 31)
                // SurfaceFlinger frame data
                appendLine("data_sources: {")
                appendLine("    config {")
                appendLine("        name: \"android.surfaceflinger.frame\"")
                appendLine("    }")
                appendLine("}")
                appendLine()
                
                // Frame timeline events (for actual_frame_timeline_event table)
                appendLine("data_sources: {")
                appendLine("    config {")
                appendLine("        name: \"android.surfaceflinger.frametimeline\"")
                appendLine("    }")
                appendLine("}")
            }
        }
    }
    
    /**
     * Check if Perfetto is available on the device.
     */
    fun isPerfettoAvailable(): Boolean {
        return try {
            val output = AdbShell.execNoRetry("shell", "perfetto", "--help", timeoutSeconds = 5)
            output.contains("Perfetto") || output.contains("perfetto")
        } catch (e: Exception) {
            logger.warn("Perfetto not available: ${e.message}")
            false
        }
    }
    
    /**
     * Get Perfetto version.
     */
    fun getPerfettoVersion(): String? {
        return try {
            val output = AdbShell.execNoRetry("shell", "perfetto", "--version", timeoutSeconds = 5)
            output.trim()
        } catch (e: Exception) {
            null
        }
    }
}

