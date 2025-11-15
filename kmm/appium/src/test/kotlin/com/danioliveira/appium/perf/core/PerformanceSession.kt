package com.danioliveira.appium.perf.core

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.metrics.android.SystraceCollector
import com.danioliveira.appium.metrics.android.PerfettoCollector
import com.danioliveira.appium.metrics.android.PerformanceStatisticsCollector
import com.danioliveira.appium.metrics.android.perfetto.PerfettoMetricsExtractor
import com.danioliveira.appium.perf.fps.AtraceChoreographer
import com.danioliveira.appium.perf.fps.FlashlightGfxInfo
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Manages the lifecycle of a performance measurement session.
 * Handles continuous trace capture, segmentation, and metrics collection.
 */
class PerformanceSession(
    private val packageName: String,
    private val config: PerformanceConfig,
    private val androidCollector: AndroidMetricsCollector? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private var systraceCollector: SystraceCollector? = null
    private var perfettoCollector: PerfettoCollector? = null
    private var statsCollector: PerformanceStatisticsCollector? = null
    private var sessionStartTime: Long = 0
    private var captureStartTime: Long = 0
    
    // Time series data
    private val cpuTimeSeries = mutableListOf<TimeSeriesPoint>()
    private val memoryTimeSeries = mutableListOf<TimeSeriesPoint>()
    private val fpsTimeSeries = mutableListOf<TimeSeriesPoint>()
    
    /**
     * Start the performance session.
     * Begins continuous trace capture and metrics polling.
     */
    fun start() {
        sessionStartTime = System.currentTimeMillis()
        
        when (config.platform) {
            Platform.ANDROID -> startAndroidSession()
            Platform.IOS -> startIosSession()
        }
    }
    
    private fun startAndroidSession() {
        if (androidCollector == null) {
            throw IllegalStateException("Android collector not available")
        }
        
        // Ensure prerequisites
        enforceAndroidPrerequisites()
        
        // Start trace capture (Perfetto or Systrace)
        if (config.usePerfetto) {
            // Use Perfetto (modern, recommended)
            perfettoCollector = PerfettoCollector()
            val started = perfettoCollector?.startCapture(config, packageName)
            
            if (started != true) {
                logger.warn("Failed to start Perfetto capture, falling back to Systrace")
                startSystraceCapture()
            } else {
                logger.info("✅ Android performance session started (Perfetto)")
            }
        } else {
            // Use Systrace (legacy)
            startSystraceCapture()
            logger.info("✅ Android performance session started (Systrace)")
        }
        
        captureStartTime = System.currentTimeMillis()
        
        // Start polling for real-time metrics
        if (config.enableCpuProfiling || config.enableMemoryProfiling || config.enableFpsProfiling) {
            statsCollector = PerformanceStatisticsCollector(androidCollector, packageName)
            statsCollector?.startPolling(intervalMs = config.pollingIntervalMs)
            logger.info("   Real-time polling started (${config.pollingIntervalMs}ms interval)")
        }
    }
    
    private fun startSystraceCapture() {
        systraceCollector = androidCollector?.systraceCollector
        val started = systraceCollector?.startCapture(
            bufferSizeKb = config.systraceBufferSizeKb,
            categories = config.systraceCategories,
            packageName = packageName
        )
        
        if (started != true) {
            throw IllegalStateException("Failed to start systrace capture")
        }
    }
    
    private fun startIosSession() {
        // TODO: Implement iOS session start with Instruments
        logger.info("✅ iOS performance session started")
        captureStartTime = System.currentTimeMillis()
    }
    
    /**
     * Stop the performance session and collect all metrics.
     */
    fun stop(testName: String = "test"): SessionData {
        val sessionEndTime = System.currentTimeMillis()
        val sessionDuration = sessionEndTime - sessionStartTime
        
        return when (config.platform) {
            Platform.ANDROID -> stopAndroidSession(testName, sessionDuration)
            Platform.IOS -> stopIosSession(testName, sessionDuration)
        }
    }
    
    private fun stopAndroidSession(testName: String, sessionDuration: Long): SessionData {
        logger.info("Stopping Android performance session...")
        
        // Stop polling and get ADB statistics
        val adbStats = statsCollector?.stopPolling()
        
        // Stop trace capture and parse
        val traceFile: File?
        val traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics?
        
        if (perfettoCollector != null) {
            // Stop Perfetto capture
            traceFile = perfettoCollector?.stopCapture(testName)
            traceMetrics = if (traceFile != null) {
                perfettoCollector?.parseTrace(traceFile)
            } else {
                null
            }
            logger.info("✅ Perfetto capture stopped")
        } else {
            // Stop Systrace capture
            traceFile = systraceCollector?.stopCapture(testName)
            traceMetrics = if (traceFile != null) {
                systraceCollector?.parseTrace(traceFile)
            } else {
                null
            }
            logger.info("✅ Systrace capture stopped")
        }
        
        // Extract systrace statistics
        val systraceStats = if (traceMetrics != null && statsCollector != null) {
            statsCollector?.extractSystraceStatistics(traceMetrics)
        } else {
            null
        }
        
        logger.info("✅ Android session stopped. Duration: ${sessionDuration}ms")
        
        return SessionData(
            platform = Platform.ANDROID,
            durationMs = sessionDuration,
            traceFile = traceFile,
            traceMetrics = traceMetrics,
            adbStats = adbStats,
            systraceStats = systraceStats,
            cpuTimeSeries = cpuTimeSeries.toList(),
            memoryTimeSeries = memoryTimeSeries.toList(),
            fpsTimeSeries = fpsTimeSeries.toList()
        )
    }
    
    private fun stopIosSession(testName: String, sessionDuration: Long): SessionData {
        // TODO: Implement iOS session stop with Instruments parsing
        logger.info("✅ iOS session stopped. Duration: ${sessionDuration}ms")
        
        return SessionData(
            platform = Platform.IOS,
            durationMs = sessionDuration,
            traceFile = null,
            traceMetrics = null,
            adbStats = null,
            systraceStats = null,
            cpuTimeSeries = emptyList(),
            memoryTimeSeries = emptyList(),
            fpsTimeSeries = emptyList()
        )
    }
    
    /**
     * Segment the session data by screen markers and expected screens.
     */
    fun segmentByScreens(
        sessionData: SessionData,
        expectedScreens: List<String>
    ): List<ScreenSegment> {
        return when (config.platform) {
            Platform.ANDROID -> segmentAndroidByScreens(sessionData, expectedScreens)
            Platform.IOS -> segmentIosByScreens(sessionData, expectedScreens)
        }
    }
    
    private fun segmentAndroidByScreens(
        sessionData: SessionData,
        expectedScreens: List<String>
    ): List<ScreenSegment> {
        val segments = mutableListOf<ScreenSegment>()
        val traceMetrics = sessionData.traceMetrics ?: return segments
        
        // Try to match expected screens with trace markers
        for (expectedScreen in expectedScreens) {
            val screenTrace = traceMetrics.screens.find { 
                it.name.contains(expectedScreen, ignoreCase = true) 
            }
            
            if (screenTrace != null) {
                // Calculate metrics for this screen's time window
                val metrics = calculateMetricsForWindow(
                    sessionData,
                    screenTrace.startTimeMs,
                    screenTrace.startTimeMs + screenTrace.durationMs
                )
                
                segments.add(
                    ScreenSegment(
                        screenName = expectedScreen,
                        startTimeMs = screenTrace.startTimeMs,
                        endTimeMs = screenTrace.startTimeMs + screenTrace.durationMs,
                        durationMs = screenTrace.durationMs,
                        metrics = metrics
                    )
                )
                
                logger.debug("  Segmented screen: $expectedScreen (${screenTrace.durationMs}ms)")
            } else {
                logger.warn("  ⚠️  Screen not found in trace: $expectedScreen")
            }
        }
        
        return segments
    }
    
    private fun segmentIosByScreens(
        sessionData: SessionData,
        expectedScreens: List<String>
    ): List<ScreenSegment> {
        // TODO: Implement iOS segmentation using os_signpost markers
        return emptyList()
    }
    
    /**
     * Calculate performance metrics for a specific time window.
     * 
     * This method tries multiple strategies:
     * 1. Perfetto SQL queries (most accurate, Android 12+)
     * 2. Systrace text parsing (fallback)
     * 3. ADB polling stats (last resort)
     */
    private fun calculateMetricsForWindow(
        sessionData: SessionData,
        startMs: Long,
        endMs: Long
    ): PerformanceMetrics {
        // Try Perfetto SQL queries first (if .pftrace file)
        val traceFile = sessionData.traceFile
        if (traceFile != null && traceFile.extension == "pftrace") {
            return calculateMetricsFromPerfetto(traceFile, startMs, endMs)
        }
        
        // Fallback to systrace parsing
        val traceMetrics = sessionData.traceMetrics
        if (traceMetrics != null) {
            return calculateMetricsFromSystrace(traceMetrics, sessionData, startMs, endMs)
        }
        
        // Last resort: use ADB stats
        return calculateMetricsFromAdb(sessionData)
    }
    
    /**
     * Calculate metrics using Perfetto SQL queries (most accurate).
     */
    private fun calculateMetricsFromPerfetto(
        traceFile: File,
        startMs: Long,
        endMs: Long
    ): PerformanceMetrics {
        return try {
            PerfettoMetricsExtractor(traceFile).use { extractor ->
                val memory = extractor.extractMemoryUsage(packageName, startMs * 1_000_000, endMs * 1_000_000)
                val cpu = extractor.extractCpuUtilization(packageName, startMs * 1_000_000, endMs * 1_000_000)
                val frames = extractor.extractFrameTiming(packageName, startMs * 1_000_000, endMs * 1_000_000)
                
                val memoryValues = memory.map { it.rssMb.toDouble() }
                val cpuPercent = if (cpu.isNotEmpty()) {
                    val totalDuration = cpu.sumOf { it.durationNs }
                    val timeWindow = (endMs - startMs) * 1_000_000
                    (totalDuration.toDouble() / timeWindow) * 100.0
                } else 0.0
                
                val avgFrameTimeMs = if (frames.isNotEmpty()) {
                    frames.map { it.durationMs }.average()
                } else 0.0
                val fps = if (avgFrameTimeMs > 0) 1000.0 / avgFrameTimeMs else 0.0
                
                logger.debug("  Metrics from Perfetto SQL: CPU=${String.format("%.1f", cpuPercent)}%, " +
                        "Memory=${memoryValues.average().toInt()}MB, FPS=${String.format("%.1f", fps)}")
                
                PerformanceMetrics(
                    cpu = MetricStats(
                        min = cpuPercent,
                        max = cpuPercent,
                        avg = cpuPercent,
                        p50 = cpuPercent,
                        p90 = cpuPercent,
                        stddev = 0.0,
                        samples = cpu.size
                    ),
                    memory = MetricStats.from(memoryValues),
                    fps = MetricStats(
                        min = fps,
                        max = fps,
                        avg = fps,
                        p50 = fps,
                        p90 = fps,
                        stddev = 0.0,
                        samples = frames.size
                    )
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract metrics from Perfetto: ${e.message}, falling back to systrace")
            // Fallback to systrace if Perfetto extraction fails
            PerformanceMetrics.empty()
        }
    }
    
    /**
     * Calculate metrics from systrace text parsing (fallback).
     */
    private fun calculateMetricsFromSystrace(
        traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics,
        sessionData: SessionData,
        startMs: Long,
        endMs: Long
    ): PerformanceMetrics {
        // Filter CPU data for this window
        val cpuValues = traceMetrics.cpuUtilization
            ?.filter { it.timestampMs in startMs..endMs }
            ?.map { it.cpuPercent } ?: emptyList()
        
        // For memory and FPS, we use the ADB stats as a proxy
        // (systrace doesn't provide per-window memory/FPS easily)
        val memoryValues = sessionData.adbStats?.memory?.let { 
            listOf(it.min.toDouble(), it.max.toDouble(), it.avg) 
        } ?: emptyList()
        
        val fpsValues = sessionData.adbStats?.fps?.let {
            listOf(it.min.toDouble(), it.max.toDouble(), it.avg)
        } ?: emptyList()
        
        return PerformanceMetrics(
            cpu = MetricStats.from(cpuValues),
            memory = MetricStats.from(memoryValues),
            fps = MetricStats.from(fpsValues)
        )
    }
    
    /**
     * Calculate metrics from ADB polling (last resort).
     */
    private fun calculateMetricsFromAdb(sessionData: SessionData): PerformanceMetrics {
        val adbStats = sessionData.adbStats ?: return PerformanceMetrics.empty()
        
        return PerformanceMetrics(
            cpu = MetricStats(
                min = adbStats.cpu.min.toDouble(),
                max = adbStats.cpu.max.toDouble(),
                avg = adbStats.cpu.avg,
                p50 = adbStats.cpu.avg,  // Approximate
                p90 = adbStats.cpu.max.toDouble(),  // Approximate
                stddev = 0.0,  // Not available from ADB stats
                samples = adbStats.cpu.samples
            ),
            memory = MetricStats(
                min = adbStats.memory.min.toDouble(),
                max = adbStats.memory.max.toDouble(),
                avg = adbStats.memory.avg,
                p50 = adbStats.memory.avg,  // Approximate
                p90 = adbStats.memory.max.toDouble(),  // Approximate
                stddev = 0.0,  // Not available from ADB stats
                samples = adbStats.memory.samples
            ),
            fps = MetricStats(
                min = adbStats.fps.min.toDouble(),
                max = adbStats.fps.max.toDouble(),
                avg = adbStats.fps.avg,
                p50 = adbStats.fps.avg,  // Approximate
                p90 = adbStats.fps.max.toDouble(),  // Approximate
                stddev = 0.0,  // Not available from ADB stats
                samples = adbStats.fps.samples
            )
        )
    }
    
    /**
     * Enforce Android prerequisites for reliable metrics.
     */
    private fun enforceAndroidPrerequisites() {
        try {
            // Enable HWUI profiling for accurate frame timing
            val adbPath = findAdbPath()
            val result = ProcessBuilder(
                adbPath, "shell", "setprop", "debug.hwui.profile", "true"
            ).start().waitFor()
            
            if (result == 0) {
                logger.info("✅ HWUI profiling enabled (debug.hwui.profile=true)")
            } else {
                logger.warn("⚠️  Failed to enable HWUI profiling")
            }
            
            // Verify 'view' category is in config (required for Choreographer)
            if (!config.systraceCategories.contains("view")) {
                logger.warn("⚠️  'view' category not in systrace config - Choreographer FPS may not work")
            }
            
        } catch (e: Exception) {
            logger.warn("⚠️  Failed to enforce prerequisites: ${e.message}")
        }
    }
    
    private fun findAdbPath(): String {
        val androidHome = System.getenv("ANDROID_HOME") 
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: System.getProperty("user.home") + "/Library/Android/sdk"
        
        return "$androidHome/platform-tools/adb"
    }
}

/**
 * Data collected during a performance session.
 */
data class SessionData(
    val platform: Platform,
    val durationMs: Long,
    val traceFile: File?,
    val traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics?,
    val adbStats: com.danioliveira.appium.metrics.android.AdbStatistics?,
    val systraceStats: com.danioliveira.appium.metrics.android.SystraceStatistics?,
    val cpuTimeSeries: List<TimeSeriesPoint>,
    val memoryTimeSeries: List<TimeSeriesPoint>,
    val fpsTimeSeries: List<TimeSeriesPoint>
)

/**
 * A screen segment within a session.
 */
data class ScreenSegment(
    val screenName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val metrics: PerformanceMetrics
)

/**
 * A time series data point.
 */
data class TimeSeriesPoint(
    val timestampMs: Long,
    val value: Double
)



