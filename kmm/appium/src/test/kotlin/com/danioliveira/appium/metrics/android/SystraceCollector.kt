package com.danioliveira.appium.metrics.android

import com.danioliveira.appium.utils.AdbShell
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Manages systrace capture for Android performance analysis.
 * Captures system-level traces including custom SysTrace markers from the app.
 * 
 * Usage:
 * ```kotlin
 * val collector = SystraceCollector()
 * collector.startCapture(durationSeconds = 30)
 * // ... run your test ...
 * val traceFile = collector.stopCapture()
 * val metrics = collector.parseTrace(traceFile)
 * ```
 */
class SystraceCollector(
    private val outputDir: File = File("build/traces/android")
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var captureProcess: Process? = null
    private var captureStartTime: Long = 0
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    
    init {
        outputDir.mkdirs()
        logger.info("SystraceCollector initialized. Output dir: ${outputDir.absolutePath}")
    }
    
    /**
     * Start systrace capture asynchronously.
     * Capture will continue until stopCapture() is called.
     * 
     * @param bufferSizeKb Buffer size in KB (default 65MB for longer captures)
     * @param categories Trace categories to capture
     * @param packageName App package name for app-level tracing
     * @return true if capture started successfully
     */
    fun startCapture(
        bufferSizeKb: Int = 65536,
        categories: List<String> = DEFAULT_CATEGORIES,
        packageName: String? = null
    ): Boolean {
        if (captureProcess != null) {
            logger.warn("Capture already in progress")
            return false
        }
        
        try {
            val adbPath = findAdbPath()
            val categoriesStr = categories.joinToString(" ")
            
            logger.info("Starting systrace capture:")
            logger.info("  Buffer: ${bufferSizeKb}KB")
            if (packageName != null) {
                logger.info("  App: $packageName (app-level tracing enabled)")
            }
            logger.info("  Categories: $categoriesStr")
            
            // Build command with app-level tracing if package name provided
            val command = mutableListOf(
                adbPath, "shell", "atrace",
                "--async_start",
                "-b", bufferSizeKb.toString()
            )
            
            // Add app-level tracing for detailed frame rendering
            if (packageName != null) {
                command.add("-a")
                command.add(packageName)
            }
            
            command.addAll(categories)
            
            logger.debug("Command: ${command.joinToString(" ")}")
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                logger.error("Failed to start systrace: $output")
                return false
            }
            
            captureStartTime = System.currentTimeMillis()
            logger.info("✅ Systrace capture started successfully")
            logger.info("   Capture will run until stopCapture() is called")
            
            // Give atrace a moment to initialize
            Thread.sleep(500)
            
            return true
            
        } catch (e: Exception) {
            logger.error("Failed to start systrace capture: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Stop systrace capture and save to file.
     * 
     * @param testName Name to include in trace filename
     * @return File containing the trace, or null if capture failed
     */
    fun stopCapture(testName: String = "trace"): File? {
        try {
            val timestamp = dateFormat.format(Date())
            val outputFile = File(outputDir, "systrace_${testName}_${timestamp}.html")
            
            logger.info("Stopping systrace capture...")
            
            val adbPath = findAdbPath()
            val command = listOf(adbPath, "shell", "atrace", "--async_stop")
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            // Read trace data
            val traceData = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                logger.error("Failed to stop systrace")
                return null
            }
            
            // Save to file
            outputFile.writeText(traceData)
            
            val durationMs = System.currentTimeMillis() - captureStartTime
            val fileSizeMb = outputFile.length() / (1024.0 * 1024.0)
            
            logger.info("✅ Systrace capture stopped")
            logger.info("   Duration: ${durationMs}ms")
            logger.info("   File: ${outputFile.name}")
            logger.info("   Size: ${String.format("%.2f", fileSizeMb)}MB")
            logger.info("   Path: ${outputFile.absolutePath}")
            
            captureProcess = null
            captureStartTime = 0
            
            return outputFile
            
        } catch (e: Exception) {
            logger.error("Failed to stop systrace capture: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Parse trace file to extract performance metrics.
     * Extracts:
     * - Screen traces (act: markers)
     * - CPU utilization (from sched_switch and cpu_idle events)
     * - App startup time (from Android App Startups)
     * 
     * @param traceFile HTML trace file from systrace
     * @return Parsed trace metrics with CPU data
     */
    fun parseTrace(traceFile: File): TraceMetrics {
        if (!traceFile.exists()) {
            logger.error("Trace file not found: ${traceFile.absolutePath}")
            return TraceMetrics.empty()
        }
        
        logger.info("Parsing trace file: ${traceFile.name}")
        
        try {
            val content = traceFile.readText()
            
            // 1. Parse screen traces (act: markers)
            val screens = parseScreenTraces(content)
            
            // 2. Parse CPU utilization
            val cpuData = parseCpuUtilization(content)
            
            // 3. Parse startup time from Android App Startups
            val startupTime = parseStartupTime(content)
            
            // Calculate total duration
            val totalDuration = if (screens.isNotEmpty()) {
                screens.maxOf { it.endTimeMs } - screens.minOf { it.startTimeMs }
            } else 0L
            
            // NEW: Extract FPS from Choreographer#doFrame events
            val (fps, frameCount) = extractFpsFromTrace(content, cpuData)
            
            // Log summary
            logger.info("✅ Trace parsing complete:")
            logger.info("   Screen traces: ${screens.size}")
            logger.info("   CPU data points: ${cpuData.size}")
            if (cpuData.isNotEmpty()) {
                logger.info("   CPU avg: ${String.format("%.1f", cpuData.map { it.cpuPercent }.average())}%")
                logger.info("   CPU peak: ${String.format("%.1f", cpuData.maxOf { it.cpuPercent })}%")
            }
            if (startupTime != null) {
                logger.info("   Startup time: ${startupTime}ms")
            }
            logger.info("   FPS: ${String.format("%.1f", fps)} (${frameCount} frames)")
            
            return TraceMetrics(
                traceFile = traceFile,
                screens = screens,
                totalDurationMs = totalDuration,
                cpuUtilization = cpuData,
                startupTimeMs = startupTime,
                fps = fps,
                frameCount = frameCount
            )
            
        } catch (e: Exception) {
            logger.error("Failed to parse trace file: ${e.message}", e)
            return TraceMetrics.empty()
        }
    }
    
    /**
     * Parse screen traces from act: markers.
     */
    private fun parseScreenTraces(content: String): List<ScreenTrace> {
        val screens = mutableListOf<ScreenTrace>()
        
        val asyncStartPattern = Regex("""atrace_async_begin\|.*?\|act:(\w+)\|(\d+)""")
        val asyncEndPattern = Regex("""atrace_async_end\|.*?\|act:(\w+)\|(\d+)""")
        
        val startEvents = mutableMapOf<String, MutableList<Pair<String, Long>>>()
        val endEvents = mutableMapOf<String, MutableList<Pair<String, Long>>>()
        
        // Extract start events
        asyncStartPattern.findAll(content).forEach { match ->
            val screenName = match.groupValues[1]
            val cookie = match.groupValues[2]
            startEvents.getOrPut(cookie) { mutableListOf() }.add(screenName to 0L)
        }
        
        // Extract end events
        asyncEndPattern.findAll(content).forEach { match ->
            val screenName = match.groupValues[1]
            val cookie = match.groupValues[2]
            endEvents.getOrPut(cookie) { mutableListOf() }.add(screenName to 0L)
        }
        
        // Match start/end events by cookie
        startEvents.forEach { (cookie, starts) ->
            val ends = endEvents[cookie]
            if (ends != null && starts.size == ends.size) {
                starts.zip(ends).forEach { (start, end) ->
                    if (start.first == end.first) {
                        screens.add(ScreenTrace(
                            name = start.first,
                            startTimeMs = start.second,
                            endTimeMs = end.second,
                            durationMs = end.second - start.second
                        ))
                    }
                }
            }
        }
        
        if (screens.isEmpty()) {
            logger.warn("No screen traces found. App may not have SysTrace instrumentation.")
        } else {
            logger.info("Found ${screens.size} screen traces:")
            screens.forEach { screen ->
                logger.info("   ${screen.name}: ${screen.durationMs}ms")
            }
        }
        
        return screens
    }
    
    /**
     * Parse CPU utilization from trace events.
     * Extracts CPU load data from sched_switch and cpu_idle events.
     */
    private fun parseCpuUtilization(content: String): List<CpuUtilization> {
        val cpuData = mutableListOf<CpuUtilization>()
        
        try {
            // Look for CPU load/frequency data in the trace
            // Format varies but typically includes cpu_frequency and cpu_idle events
            
            // Pattern 1: CPU frequency changes (indicates activity)
            val freqPattern = Regex("""cpu_frequency.*?cpu=(\d+).*?state=(\d+).*?(\d+\.\d+)""")
            
            // Pattern 2: CPU idle state changes
            val idlePattern = Regex("""cpu_idle.*?state=(\d+).*?cpu_id=(\d+).*?(\d+\.\d+)""")
            
            // Pattern 3: sched_switch events (process scheduling)
            val schedPattern = Regex("""sched_switch.*?prev_pid=(\d+).*?next_pid=(\d+).*?(\d+\.\d+)""")
            
            // Sample CPU load at regular intervals from the trace data
            // This is a simplified approach - for production, you'd want to use
            // Perfetto's trace processor or parse the JSON export
            
            val cpuLoadPattern = Regex("""CpuFreq.*?(\d+\.\d+).*?(\d+)%""")
            cpuLoadPattern.findAll(content).forEach { match ->
                val timestamp = match.groupValues[1].toDoubleOrNull()
                val percent = match.groupValues[2].toDoubleOrNull()
                
                if (timestamp != null && percent != null) {
                    cpuData.add(CpuUtilization(
                        timestampMs = (timestamp * 1000).toLong(),
                        cpuPercent = percent
                    ))
                }
            }
            
            // If no direct CPU load found, estimate from scheduling activity
            if (cpuData.isEmpty()) {
                logger.info("No direct CPU load data found, estimating from scheduling events...")
                cpuData.addAll(estimateCpuFromScheduling(content))
            }
            
        } catch (e: Exception) {
            logger.warn("Failed to parse CPU utilization: ${e.message}")
        }
        
        return cpuData.sortedBy { it.timestampMs }
    }
    
    /**
     * Estimate CPU utilization from scheduling events.
     * This is a fallback when direct CPU load data isn't available.
     */
    private fun estimateCpuFromScheduling(content: String): List<CpuUtilization> {
        val estimates = mutableListOf<CpuUtilization>()
        
        try {
            // Count scheduling events in time windows to estimate CPU activity
            val schedPattern = Regex("""sched_switch.*?(\d+\.\d+)""")
            val timestamps = schedPattern.findAll(content)
                .mapNotNull { it.groupValues[1].toDoubleOrNull() }
                .sorted()
                .toList()
            
            if (timestamps.isEmpty()) {
                return emptyList()
            }
            
            // Group into 100ms windows and count events
            val windowSize = 0.1 // 100ms
            val windows = mutableMapOf<Long, Int>()
            
            timestamps.forEach { ts ->
                val window = ((ts / windowSize).toLong() * windowSize * 1000).toLong()
                windows[window] = (windows[window] ?: 0) + 1
            }
            
            // Convert event counts to estimated CPU percentage
            // More events = higher CPU usage (rough approximation)
            val maxEvents = windows.values.maxOrNull() ?: 1
            windows.forEach { (window, count) ->
                val estimatedPercent = (count.toDouble() / maxEvents) * 100.0
                estimates.add(CpuUtilization(
                    timestampMs = window,
                    cpuPercent = estimatedPercent.coerceIn(0.0, 100.0)
                ))
            }
            
        } catch (e: Exception) {
            logger.warn("Failed to estimate CPU from scheduling: ${e.message}")
        }
        
        return estimates
    }
    
    /**
     * Parse app startup time from android_startup metric in the trace.
     * This is the official Android metric that Perfetto extracts.
     * 
     * The metric measures time from app launch intent to first frame displayed.
     */
    private fun parseStartupTime(content: String): Long? {
        try {
            // Strategy 1: Look for android_startup metric (most accurate)
            // Format: "android_startup" with duration in nanoseconds
            val androidStartupPattern = Regex("""android_startup.*?(\d+(?:\.\d+)?)\s*ms""", RegexOption.IGNORE_CASE)
            val startupMatch = androidStartupPattern.find(content)
            
            if (startupMatch != null) {
                val durationMs = startupMatch.groupValues[1].toDoubleOrNull()
                if (durationMs != null) {
                    logger.info("Found android_startup metric: ${durationMs}ms")
                    return durationMs.toLong()
                }
            }
            
            // Strategy 2: Look for startup duration in nanoseconds
            val startupNsPattern = Regex("""android_startup.*?dur[:\s=]+(\d+)""", RegexOption.IGNORE_CASE)
            val nsMatch = startupNsPattern.find(content)
            
            if (nsMatch != null) {
                val durNs = nsMatch.groupValues[1].toLongOrNull()
                if (durNs != null && durNs > 1_000_000) { // Sanity check: > 1ms
                    val durMs = durNs / 1_000_000
                    logger.info("Found android_startup metric: ${durMs}ms (from ns)")
                    return durMs
                }
            }
            
            // Strategy 3: Look for "launching" events with duration
            val launchingPattern = Regex("""launching.*?(\d+(?:\.\d+)?)\s*ms""", RegexOption.IGNORE_CASE)
            val launchMatch = launchingPattern.find(content)
            
            if (launchMatch != null) {
                val durationMs = launchMatch.groupValues[1].toDoubleOrNull()
                if (durationMs != null) {
                    logger.info("Found launching metric: ${durationMs}ms")
                    return durationMs.toLong()
                }
            }
            
            // Strategy 4: Look for activityStart to activityResume duration
            val activityStartPattern = Regex("""activityStart.*?ts[:\s=]+(\d+\.\d+)""")
            val activityResumePattern = Regex("""activityResume.*?ts[:\s=]+(\d+\.\d+)""")
            
            val startMatch = activityStartPattern.find(content)
            val resumeMatch = activityResumePattern.find(content)
            
            if (startMatch != null && resumeMatch != null) {
                val startTs = startMatch.groupValues[1].toDoubleOrNull()
                val resumeTs = resumeMatch.groupValues[1].toDoubleOrNull()
                
                if (startTs != null && resumeTs != null) {
                    val durationMs = ((resumeTs - startTs) * 1000).toLong()
                    if (durationMs > 0 && durationMs < 60000) { // Sanity check: 0-60s
                        logger.info("Calculated startup from activity events: ${durationMs}ms")
                        return durationMs
                    }
                }
            }
            
            logger.warn("Could not find android_startup metric in trace")
            logger.info("Tip: Open trace in Perfetto and look for 'Android App Startups' section")
            
        } catch (e: Exception) {
            logger.warn("Failed to parse startup time: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Convenience method: capture trace for the duration of a test block.
     * 
     * @param testName Name for the trace file
     * @param durationSeconds How long to capture
     * @param block Test code to execute during capture
     * @return Parsed trace metrics
     */
    fun captureTrace(
        testName: String,
        durationSeconds: Int = 30,
        block: () -> Unit
    ): TraceMetrics {
        if (!startCapture(durationSeconds)) {
            logger.error("Failed to start capture")
            return TraceMetrics.empty()
        }
        
        try {
            // Execute test
            block()
            
            // Wait for capture to complete
            logger.info("Waiting for trace capture to complete...")
            Thread.sleep(durationSeconds * 1000L)
            
        } finally {
            // Stop and parse
            val traceFile = stopCapture(testName)
            return if (traceFile != null) {
                parseTrace(traceFile)
            } else {
                TraceMetrics.empty()
            }
        }
    }
    
    /**
     * Check if systrace is available on the connected device.
     */
    fun isAvailable(): Boolean {
        return try {
            val adbPath = findAdbPath()
            val process = ProcessBuilder(adbPath, "shell", "atrace", "--help")
                .redirectErrorStream(true)
                .start()
            
            val exitCode = process.waitFor()
            exitCode == 0
            
        } catch (e: Exception) {
            logger.warn("Systrace not available: ${e.message}")
            false
        }
    }
    
    /**
     * Get API level of connected device.
     * Systrace async traces require API 29+ (Android 10+)
     */
    fun getDeviceApiLevel(): Int {
        return try {
            val adbPath = findAdbPath()
            val process = ProcessBuilder(adbPath, "shell", "getprop", "ro.build.version.sdk")
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            
            output.toIntOrNull() ?: 0
            
        } catch (e: Exception) {
            logger.warn("Could not get device API level: ${e.message}")
            0
        }
    }
    
    private fun findAdbPath(): String {
        val possiblePaths = listOf(
            "adb",
            "/usr/local/bin/adb",
            System.getenv("ANDROID_HOME")?.let { "$it/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Library/Android/sdk/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Android/Sdk/platform-tools/adb" }
        ).filterNotNull()
        
        for (path in possiblePaths) {
            try {
                val process = ProcessBuilder(path, "version")
                    .redirectErrorStream(true)
                    .start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    return path
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        throw IllegalStateException("ADB not found. Please install Android SDK and set ANDROID_HOME.")
    }
    
    companion object {
        /**
         * Default trace categories for comprehensive UI performance analysis.
         * Based on actual available categories from `adb shell atrace --list_categories`.
         * 
         * Key categories for performance analysis:
         * - sched: CPU scheduling (REQUIRED for task names)
         * - freq/idle: CPU frequency and idle states
         * - gfx/view: Graphics and UI rendering
         * - memory: Memory operations and pressure
         * - sync: Synchronization primitives
         */
        val DEFAULT_CATEGORIES = listOf(
            "sched",          // CPU scheduling (REQUIRED for task names)
            "freq",           // CPU frequency changes
            "idle",           // CPU idle states
            "am",             // Activity Manager
            "wm",             // Window Manager
            "gfx",            // Graphics rendering
            "view",           // View system
            "binder_driver",  // Binder IPC
            "hal",            // Hardware Abstraction Layer
            "dalvik",         // Dalvik VM
            "input",          // Input events
            "res",            // Resource loading
            "sync",           // Synchronization
            "memory"          // Memory operations
        )
        
        /**
         * Minimal categories for quick traces (smaller file size).
         * Use for rapid iteration during development.
         */
        val MINIMAL_CATEGORIES = listOf(
            "sched",          // CPU scheduling (REQUIRED)
            "gfx",            // Graphics
            "view",           // View system
            "am"              // Activity Manager
        )
        
        /**
         * Extended categories for deep analysis (larger file size, may require root).
         * Adds I/O, power, and thermal monitoring.
         */
        val EXTENDED_CATEGORIES = DEFAULT_CATEGORIES + listOf(
            "disk",           // Disk I/O (may require root)
            "network",        // Network operations
            "database",       // Database queries
            "power",          // Power management
            "thermal",        // Thermal events
            "irq",            // Interrupt events
            "memreclaim"      // Memory reclaim
        )
        
        /**
         * Performance-focused categories for frame drop analysis.
         * Optimized for identifying rendering issues and jank.
         */
        val PERFORMANCE_CATEGORIES = listOf(
            "sched",          // CPU scheduling
            "freq",           // CPU frequency
            "idle",           // CPU idle
            "irq",            // Interrupts
            "gfx",            // Graphics
            "view",           // View system
            "rs",             // RenderScript
            "am",             // Activity Manager
            "wm",             // Window Manager
            "input",          // Input events
            "memory",         // Memory
            "sync"            // Synchronization
        )
    }
    
    /**
     * Extract FPS from systrace using Choreographer#doFrame events.
     * Uses Flashlight's atrace algorithm with UI CPU adjustment.
     * 
     * Reference: https://github.com/bamlab/flashlight/blob/main/packages/platforms/android/src/commands/atrace/pollFpsUsage.ts
     * 
     * @param content Raw systrace content
     * @param cpuData CPU utilization data for UI thread adjustment
     * @return Pair of (FPS, frameCount)
     */
    private fun extractFpsFromTrace(
        content: String,
        cpuData: List<CpuUtilization>
    ): Pair<Double, Int> {
        try {
            // Extract PID from trace
            val pid = getPidFromTrace(content) ?: run {
                logger.warn("Could not extract PID from trace for FPS calculation")
                return 0.0 to 0
            }
            
            // Parse frame times using atrace parser
            val parser = AtraceFrameParser()
            val result = parser.getFrameTimes(content, pid)
            
            if (result.frameTimes.isEmpty()) {
                logger.debug("No Choreographer#doFrame events found in trace")
                return 0.0 to 0
            }
            
            // Calculate UI thread CPU usage for idle adjustment
            val uiCpuUsage = if (cpuData.isNotEmpty()) {
                cpuData.map { it.cpuPercent }.average()
            } else {
                0.0
            }
            
            // Calculate FPS using Flashlight's algorithm
            val fps = AtraceFrameParser.getFps(
                frameTimes = result.frameTimes,
                timeInterval = result.interval,
                uiCpuUsage = uiCpuUsage
            )
            
            logger.debug("Extracted FPS from systrace:")
            logger.debug("  Frame count: ${result.frameTimes.size}")
            logger.debug("  Interval: ${String.format("%.0f", result.interval)}ms")
            logger.debug("  UI CPU usage: ${String.format("%.1f", uiCpuUsage)}%")
            logger.debug("  FPS: ${String.format("%.1f", fps)}")
            
            return fps to result.frameTimes.size
            
        } catch (e: Exception) {
            logger.warn("Failed to extract FPS from trace: ${e.message}")
            return 0.0 to 0
        }
    }
    
    /**
     * Extract PID from trace content.
     * Looks for process lines with the package name.
     */
    private fun getPidFromTrace(content: String): String? {
        // Try to find PID from process lines
        // Format: "process-pid [cpu] timestamp: event"
        val pidPattern = Regex("""(\w+)-(\d+)\s+\[""")
        val match = pidPattern.find(content)
        return match?.groupValues?.get(2)
    }
}

/**
 * Represents a single screen trace from the app.
 */
data class ScreenTrace(
    val name: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long
)

/**
 * CPU utilization data point from trace.
 */
data class CpuUtilization(
    val timestampMs: Long,
    val cpuPercent: Double,
    val core: Int? = null  // Specific CPU core, or null for overall
)

/**
 * Per-screen performance metrics.
 * Contains CPU, Memory, and FPS data for a specific screen.
 */
data class ScreenMetrics(
    val screenName: String,
    val durationMs: Long,
    val startMs: Long,
    val endMs: Long,
    val cpuMin: Double = 0.0,
    val cpuMax: Double = 0.0,
    val cpuAvg: Double = 0.0,
    val cpuSamples: Int = 0,
    val memoryMin: Int = 0,
    val memoryMax: Int = 0,
    val memoryAvg: Int = 0,
    val memoryP50: Int = 0,
    val memoryP90: Int = 0,
    val memorySamples: Int = 0,
    val fpsMin: Double = 0.0,
    val fpsMax: Double = 0.0,
    val fpsAvg: Double = 0.0,
    val frameCount: Int = 0,
    val jankCount: Int = 0
) {
    val jankPercentage: Double
        get() = if (frameCount > 0) (jankCount.toDouble() / frameCount) * 100.0 else 0.0
}

/**
 * Per-second FPS data point for time-series analysis.
 */
data class FpsPerSecond(
    val second: Long,  // Timestamp in seconds from trace start
    val fps: Int       // Frame count in that second
)

/**
 * Complete trace metrics from a systrace capture.
 * Includes screen traces and CPU utilization data.
 */
data class TraceMetrics(
    val traceFile: File?,
    val screens: List<ScreenTrace>,
    val totalDurationMs: Long,
    val cpuUtilization: List<CpuUtilization> = emptyList(),
    val startupTimeMs: Long? = null,  // From Android App Startups section
    val fps: Double = 0.0,  // From Choreographer#doFrame events (atrace)
    val frameCount: Int = 0,  // Number of frames rendered
    val screenMetrics: List<ScreenMetrics> = emptyList(),  // Per-screen metrics
    val fpsPerSecond: List<FpsPerSecond> = emptyList()  // Per-second FPS time series
) {
    val screenCount: Int get() = screens.size
    
    val avgScreenDurationMs: Double
        get() = if (screens.isNotEmpty()) {
            screens.map { it.durationMs }.average()
        } else 0.0
    
    val avgCpuPercent: Double
        get() = if (cpuUtilization.isNotEmpty()) {
            cpuUtilization.map { it.cpuPercent }.average()
        } else 0.0
    
    val peakCpuPercent: Double
        get() = cpuUtilization.maxOfOrNull { it.cpuPercent } ?: 0.0
    
    fun getScreenByName(name: String): ScreenTrace? {
        return screens.firstOrNull { it.name == name }
    }
    
    fun getScreenMetricsByName(name: String): ScreenMetrics? {
        return screenMetrics.firstOrNull { it.screenName == name }
    }
    
    fun getScreenDurations(): Map<String, List<Long>> {
        return screens.groupBy { it.name }
            .mapValues { (_, traces) -> traces.map { it.durationMs } }
    }
    
    fun getCpuUtilizationInRange(startMs: Long, endMs: Long): List<CpuUtilization> {
        return cpuUtilization.filter { it.timestampMs in startMs..endMs }
    }
    
    companion object {
        fun empty() = TraceMetrics(null, emptyList(), 0L, emptyList(), null)
    }
}

