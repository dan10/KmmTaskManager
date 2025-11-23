package com.danioliveira.appium.metrics

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.metrics.ios.IOSMetricsCollector
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

interface MetricsManager {
    fun measureAppLaunchTime(): Long
    fun startPerformanceRecording()
    fun stopPerformanceRecording(testName: String = "test"): String?
    fun trackAction(pageName: String, actionName: String, block: () -> Unit)
    fun getActionMetrics(): List<ActionMetrics>
    fun analyzeTraceSegments(): Map<String, TraceSegmentMetrics>
    fun reset()
    fun getPageSummary(pageName: String): PageMetricsSummary?
    fun getTotalSummary(): TotalMetricsSummary
    fun getTraceMetrics(): com.danioliveira.appium.metrics.android.TraceMetrics?
    fun getAppLaunchTime(): Long
}

class LegacyMetricsManager(
    private val platform: Platform,
    private val packageOrBundleId: String,
    private val udid: String? = null,
    private val driver: WebDriver? = null,
    private val instrumentsProfileName: String = "Activity Monitor"  // iOS Instruments profile to use
) : MetricsManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    val androidCollector = if (platform == Platform.ANDROID) {
        try {
            AndroidMetricsCollector(packageOrBundleId).also {
                logger.info("✅ Android metrics collector initialized for: $packageOrBundleId")
            }
        } catch (e: IllegalStateException) {
            logger.error("❌ Failed to initialize Android metrics collector: ${e.message}")
            logger.error("   All metrics will be zeros. Please fix ADB setup to collect real metrics.")
            null
        }
    } else null
    
    private val iosCollector = if (platform == Platform.IOS) {
        IOSMetricsCollector(
            packageOrBundleId, 
            udid,
            driver as? IOSDriver,
            instrumentsProfileName
        ).also {
            logger.info("✅ iOS metrics collector initialized for: $packageOrBundleId")
            logger.info("   Using Instruments profile: '$instrumentsProfileName'")
        }
    } else null
    
    private val actionMetrics = mutableListOf<ActionMetrics>()
    private val pageMetricsMap = ConcurrentHashMap<String, MutableList<ActionMetrics>>()
    
    private var appLaunchTimeMs: Long = 0
    private var traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics? = null
    
    // Trace timing for correlation with action timestamps
    private var traceStartTimeMs: Long = 0  // When trace recording started (host time)
    private var traceStartTimeDeviceMs: Long = 0  // Device time when trace started (for alignment)
    
    override fun measureAppLaunchTime(): Long {
        appLaunchTimeMs = when (platform) {
            Platform.ANDROID -> androidCollector?.measureLaunchTime() ?: 0L
            Platform.IOS -> iosCollector?.measureLaunchTime() ?: 0L
        }
        logger.info("App launch time measured: ${appLaunchTimeMs}ms")
        return appLaunchTimeMs
    }

    override fun getAppLaunchTime(): Long {
        return appLaunchTimeMs
    }
    
    /**
     * Start performance recording
     * - iOS: Uses Instruments
     * - Android: Starts systrace capture with app-level tracing
     * 
     * Capture will continue until stopPerformanceRecording() is called.
     * Records the start time for trace-to-action correlation.
     */
    override fun startPerformanceRecording() {
        traceStartTimeMs = System.currentTimeMillis()
        
        when (platform) {
            Platform.IOS -> {
                iosCollector?.startPerformanceRecording()
                // For iOS, we'll use the trace file's timestamp when parsing
                logger.info("✅ Instruments recording started at host time: $traceStartTimeMs")
            }
            Platform.ANDROID -> {
                // Start systrace with app-level tracing enabled
                val started = androidCollector?.systraceCollector?.startCapture(
                    packageName = packageOrBundleId
                )
                if (started == true) {
                    // For now, use host time (device time alignment can be added later if needed)
                    traceStartTimeDeviceMs = traceStartTimeMs
                    logger.info("✅ Systrace capture started at host time: $traceStartTimeMs")
                } else {
                    logger.warn("⚠️  Failed to start systrace capture")
                }
            }
        }
    }
    
    /**
     * Stop performance recording
     * - iOS: Stops Instruments and parses trace
     * - Android: Stops systrace and parses trace
     * 
     * @param testName Name for the trace file
     * @return Summary message
     */
    override fun stopPerformanceRecording(testName: String): String? {
        return when (platform) {
            Platform.IOS -> {
                val snapshot = iosCollector?.stopPerformanceRecording()
                
                // Update all collected metrics with parsed values from trace
                iosCollector?.let { collector ->
                    updateMetricsWithParsedValues(collector)
                }
                
                snapshot?.message
            }
            Platform.ANDROID -> {
                val traceFile = androidCollector?.systraceCollector?.stopCapture(testName)
                if (traceFile != null) {
                    traceMetrics = androidCollector?.systraceCollector?.parseTrace(traceFile)
                    logger.info("✅ Systrace captured: ${traceFile.name}")
                    logger.info("   Found ${traceMetrics?.screenCount ?: 0} screen traces")
                    "Systrace saved to: ${traceFile.absolutePath}"
                } else {
                    logger.warn("⚠️  Failed to capture systrace")
                    null
                }
            }
        }
    }
    
    /**
     * Update all collected metrics with real values from parsed trace file
     * This replaces placeholder 0 values with actual parsed metrics
     */
    private fun updateMetricsWithParsedValues(collector: IOSMetricsCollector) {
        val parsedMetrics = collector.getLastParsedMetrics() ?: return
        
        logger.info("Updating ${actionMetrics.size} action metrics with parsed trace values...")
        logger.info("  Parsed CPU: avg=${String.format("%.1f", parsedMetrics.avgCpuPercent)}%, peak=${String.format("%.1f", parsedMetrics.peakCpuPercent)}%")
        logger.info("  Parsed Memory: avg=${parsedMetrics.avgMemoryMb}MB, peak=${parsedMetrics.peakMemoryMb}MB")
        
        // Update all action metrics with the parsed values
        actionMetrics.forEachIndexed { index, metrics ->
            actionMetrics[index] = metrics.copy(
                memoryMb = parsedMetrics.avgMemoryMb,
                cpuPercent = parsedMetrics.avgCpuPercent
            )
        }
        
        // Update page metrics map
        pageMetricsMap.forEach { (pageName, metricsList) ->
            pageMetricsMap[pageName] = metricsList.map { metrics ->
                metrics.copy(
                    memoryMb = parsedMetrics.avgMemoryMb,
                    cpuPercent = parsedMetrics.avgCpuPercent
                )
            }.toMutableList()
        }
        
        logger.info("✅ All metrics updated with real trace values")
    }
    
    override fun trackAction(pageName: String, actionName: String, block: () -> Unit) {
        logger.debug("Starting metrics collection for $pageName.$actionName")
        
        val startTimeMs = System.currentTimeMillis()
        val startTimeRelativeToTrace = startTimeMs - traceStartTimeMs
        
        // Collect BEFORE metrics
        val beforeMetrics = when (platform) {
            Platform.ANDROID -> {
                androidCollector?.let {
                    // Reset frame stats before measuring
                    it.resetGfxInfo()
                    MetricsSnapshot(
                        memoryMb = it.collectMemInfo().rssMb,
                        cpuPercent = it.collectCpuInfo().cpuPercentage
                    )
                } ?: MetricsSnapshot(0, 0.0)
            }
            Platform.IOS -> {
                iosCollector?.let {
                    MetricsSnapshot(
                        memoryMb = it.collectMemoryInfo().avgMemoryMb,
                        cpuPercent = it.collectCpuInfo().avgCpuPercent
                    )
                } ?: MetricsSnapshot(0, 0.0)
            }
        }
        
        logger.debug("Before metrics: memory=${beforeMetrics.memoryMb}MB, cpu=${beforeMetrics.cpuPercent}%")
        
        // Execute the action
        block()
        
        // Small delay to let metrics settle
        Thread.sleep(100)
        
        val endTimeMs = System.currentTimeMillis()
        val endTimeRelativeToTrace = endTimeMs - traceStartTimeMs
        val durationMs = endTimeMs - startTimeMs
        
        // Collect AFTER metrics
        val afterMetrics = collectMetrics(pageName, actionName, durationMs)
        
        // Calculate DELTA (change during action)
        val deltaMemoryMb = afterMetrics.memoryMb - beforeMetrics.memoryMb
        val deltaCpuPercent = afterMetrics.cpuPercent - beforeMetrics.cpuPercent
        
        // Update metrics with delta information and timestamps
        val metricsWithDelta = afterMetrics.copy(
            memoryMb = afterMetrics.memoryMb,  // Keep absolute value
            cpuPercent = afterMetrics.cpuPercent,  // Keep absolute value
            deltaMemoryMb = deltaMemoryMb,  // Store delta
            deltaCpuPercent = deltaCpuPercent,  // Store delta
            startTimeMs = startTimeMs,  // Absolute host time
            endTimeMs = endTimeMs,  // Absolute host time
            startTimeRelativeToTraceMs = startTimeRelativeToTrace,  // Relative to trace start
            endTimeRelativeToTraceMs = endTimeRelativeToTrace  // Relative to trace start
        )
        
        // Store metrics
        actionMetrics.add(metricsWithDelta)
        pageMetricsMap.getOrPut(pageName) { mutableListOf() }.add(metricsWithDelta)
        
        logger.info("Metrics collected for $pageName.$actionName:")
        logger.info("  Duration: ${durationMs}ms")
        logger.info("  Time window: ${startTimeRelativeToTrace}ms - ${endTimeRelativeToTrace}ms (relative to trace)")
        logger.info("  Memory: ${afterMetrics.memoryMb}MB (Δ ${if (deltaMemoryMb >= 0) "+" else ""}${deltaMemoryMb}MB)")
        logger.info("  CPU: ${String.format("%.1f", afterMetrics.cpuPercent)}% (Δ ${if (deltaCpuPercent >= 0) "+" else ""}${String.format("%.1f", deltaCpuPercent)}%)")
        logger.info("  FPS: ${String.format("%.1f", afterMetrics.fps)}")
    }
    
    /**
     * Snapshot of metrics at a point in time.
     */
    private data class MetricsSnapshot(
        val memoryMb: Int,
        val cpuPercent: Double
    )
    
    private fun collectMetrics(
        pageName: String, 
        actionName: String, 
        durationMs: Long
    ): ActionMetrics {
        return when (platform) {
            Platform.ANDROID -> collectAndroidMetrics(pageName, actionName, durationMs)
            Platform.IOS -> collectIOSMetrics(pageName, actionName, durationMs)
        }
    }
    
    private fun collectAndroidMetrics(
        pageName: String,
        actionName: String,
        durationMs: Long
    ): ActionMetrics {
        if (androidCollector == null) {
            logger.warn("⚠️  Android collector not available. Returning empty metrics.")
            return ActionMetrics(
                pageName = pageName,
                actionName = actionName,
                durationMs = durationMs,
                memoryMb = 0,
                cpuPercent = 0.0,
                avgFrameTimeMs = 0.0,
                jankPercentage = 0.0,
                fps = 0.0,
                platform = platform.name
            )
        }
        
        val gfxInfo = androidCollector.collectGfxInfo()
        val memInfo = androidCollector.collectMemInfo()
        val cpuInfo = androidCollector.collectCpuInfo()
        
        // Log warning if metrics are all zeros (likely ADB issue)
        if (memInfo.rssMb == 0 && cpuInfo.cpuPercentage == 0.0) {
            logger.warn("⚠️  Metrics are zeros for $pageName.$actionName - ADB may not be working properly")
        }
        
        return ActionMetrics(
            pageName = pageName,
            actionName = actionName,
            durationMs = durationMs,
            memoryMb = memInfo.rssMb,
            cpuPercent = cpuInfo.cpuPercentage,
            avgFrameTimeMs = gfxInfo.avgFrameTimeMs,
            jankPercentage = gfxInfo.jankPercentage,
            fps = if (gfxInfo.avgFrameTimeMs > 0) {
                1000.0 / gfxInfo.avgFrameTimeMs
            } else 60.0,
            platform = platform.name
        )
    }
    
    private fun collectIOSMetrics(
        pageName: String,
        actionName: String,
        durationMs: Long
    ): ActionMetrics {
        val memInfo = iosCollector?.collectMemoryInfo()
        val cpuInfo = iosCollector?.collectCpuInfo()
        val renderInfo = iosCollector?.collectRenderingMetrics()
        
        return ActionMetrics(
            pageName = pageName,
            actionName = actionName,
            durationMs = durationMs,
            memoryMb = memInfo?.peakMemoryMb ?: 0,
            cpuPercent = cpuInfo?.avgCpuPercent ?: 0.0,
            avgFrameTimeMs = renderInfo?.avgFrameTimeMs ?: 16.67,
            jankPercentage = 0.0, // iOS doesn't have direct jank metric
            fps = renderInfo?.fps ?: 60.0,
            platform = platform.name
        )
    }
    
    
    override fun getPageSummary(pageName: String): PageMetricsSummary? {
        val metrics = pageMetricsMap[pageName] ?: return null
        if (metrics.isEmpty()) return null
        
        return PageMetricsSummary(
            pageName = pageName,
            actionCount = metrics.size,
            totalDurationMs = metrics.sumOf { it.durationMs },
            avgDurationMs = metrics.map { it.durationMs }.average(),
            avgMemoryMb = metrics.map { it.memoryMb }.average(),
            peakMemoryMb = metrics.maxOf { it.memoryMb },
            avgCpuPercent = metrics.map { it.cpuPercent }.average(),
            peakCpuPercent = metrics.maxOf { it.cpuPercent },
            avgFrameTimeMs = metrics.map { it.avgFrameTimeMs }.average(),
            avgJankPercentage = metrics.map { it.jankPercentage }.average(),
            avgFps = metrics.map { it.fps }.average(),
            actions = metrics.toList()
        )
    }
    
    fun getAllPageSummaries(): List<PageMetricsSummary> {
        return pageMetricsMap.keys.mapNotNull { getPageSummary(it) }
    }
    
    override fun getTotalSummary(): TotalMetricsSummary {
        val allMetrics = actionMetrics
        
        return TotalMetricsSummary(
            appLaunchTimeMs = appLaunchTimeMs,
            totalActions = allMetrics.size,
            totalDurationMs = allMetrics.sumOf { it.durationMs },
            avgActionDurationMs = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.durationMs }.average()
            } else 0.0,
            avgMemoryMb = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.memoryMb }.average()
            } else 0.0,
            peakMemoryMb = allMetrics.maxOfOrNull { it.memoryMb } ?: 0,
            avgCpuPercent = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.cpuPercent }.average()
            } else 0.0,
            peakCpuPercent = allMetrics.maxOfOrNull { it.cpuPercent } ?: 0.0,
            avgFrameTimeMs = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.avgFrameTimeMs }.average()
            } else 0.0,
            avgJankPercentage = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.jankPercentage }.average()
            } else 0.0,
            avgFps = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.fps }.average()
            } else 60.0,
            platform = platform.name,
            pageCount = pageMetricsMap.size,
            pageSummaries = getAllPageSummaries(),
            p90CpuPercent = 0.0,  // Legacy manager doesn't have percentile data
            p95CpuPercent = 0.0,
            p99CpuPercent = 0.0,
            p90MemoryMb = 0.0,
            p95MemoryMb = 0.0,
            p99MemoryMb = 0.0,
            p90Fps = 0.0,
            p95Fps = 0.0,
            p99Fps = 0.0
        )
    }
    
    /**
     * Get systrace metrics (Android only).
     * Returns screen-level trace data from systrace capture.
     */
    override fun getTraceMetrics(): com.danioliveira.appium.metrics.android.TraceMetrics? {
        return traceMetrics
    }
    
    /**
     * Get all action-level metrics with delta values.
     */
    override fun getActionMetrics(): List<ActionMetrics> {
        return actionMetrics.toList()
    }
    
    /**
     * Get trace start time for correlation with action timestamps.
     */
    fun getTraceStartTimeMs(): Long = traceStartTimeMs
    
    /**
     * Get device trace start time (if available).
     */
    fun getTraceStartTimeDeviceMs(): Long = traceStartTimeDeviceMs
    
    /**
     * Analyze trace segments for each action using timestamps.
     * This method queries the parsed trace data for metrics within each action's time window.
     * 
     * @return Map of action key (pageName.actionName) to trace-derived metrics
     */
    override fun analyzeTraceSegments(): Map<String, TraceSegmentMetrics> {
        val results = mutableMapOf<String, TraceSegmentMetrics>()
        
        if (traceMetrics == null) {
            logger.warn("⚠️  No trace metrics available for segment analysis")
            return results
        }
        
        // Group actions by pageName.actionName
        val actionGroups = actionMetrics.groupBy { "${it.pageName}.${it.actionName}" }
        
        actionGroups.forEach { (actionKey, metricsList) ->
            // For each action, analyze all occurrences
            val segmentMetrics = metricsList.mapNotNull { metrics ->
                analyzeSingleTraceSegment(metrics)
            }
            
            if (segmentMetrics.isNotEmpty()) {
                // Aggregate metrics across all occurrences of this action
                val aggregated = aggregateTraceSegments(segmentMetrics)
                results[actionKey] = aggregated
            }
        }
        
        logger.info("✅ Analyzed ${results.size} action segments from trace")
        return results
    }
    
    /**
     * Analyze a single action's trace segment.
     */
    private fun analyzeSingleTraceSegment(metrics: ActionMetrics): TraceSegmentMetrics? {
        // Convert relative time to trace time (if we have device time alignment)
        // For now, use relative time directly - trace parsers should handle this
        val traceStart = metrics.startTimeRelativeToTraceMs
        val traceEnd = metrics.endTimeRelativeToTraceMs
        
        // Query trace metrics for this time window
        // This will be implemented in the trace collector
        return when (platform) {
            Platform.ANDROID -> {
                traceMetrics?.let { queryAndroidTraceSegment(it, traceStart, traceEnd) }
            }
            Platform.IOS -> {
                // iOS trace analysis would go here
                null
            }
        }
    }
    
    /**
     * Query Android trace metrics for a specific time window.
     */
    private fun queryAndroidTraceSegment(
        trace: com.danioliveira.appium.metrics.android.TraceMetrics,
        startMs: Long,
        endMs: Long
    ): TraceSegmentMetrics? {
        // Use TraceMetrics queryTraceSegment method directly
        return trace.queryTraceSegment(startMs, endMs)
    }
    
    /**
     * Aggregate multiple trace segments for the same action.
     */
    private fun aggregateTraceSegments(segments: List<TraceSegmentMetrics>): TraceSegmentMetrics {
        return TraceSegmentMetrics(
            avgCpuPercent = segments.map { it.avgCpuPercent }.average(),
            peakCpuPercent = segments.maxOf { it.peakCpuPercent },
            avgMemoryMb = segments.map { it.avgMemoryMb }.average().toInt(),
            peakMemoryMb = segments.maxOf { it.peakMemoryMb },
            avgFps = segments.map { it.avgFps }.average(),
            minFps = segments.minOf { it.minFps },
            jankCount = segments.sumOf { it.jankCount }
        )
    }
    
    override fun reset() {
        actionMetrics.clear()
        pageMetricsMap.clear()
        appLaunchTimeMs = 0
        traceMetrics = null
        traceStartTimeMs = 0
        traceStartTimeDeviceMs = 0
        logger.info("Metrics manager reset")
    }
}

/**
 * Metrics extracted from trace file for a specific time segment.
 */
data class TraceSegmentMetrics(
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgMemoryMb: Int,
    val peakMemoryMb: Int,
    val avgFps: Double,
    val minFps: Double,
    val jankCount: Int,
    // Percentiles
    val p50Cpu: Double = 0.0,
    val p90Cpu: Double = 0.0,
    val p95Cpu: Double = 0.0,
    val p99Cpu: Double = 0.0,
    val p50Memory: Double = 0.0,
    val p90Memory: Double = 0.0,
    val p95Memory: Double = 0.0,
    val p99Memory: Double = 0.0,
    val p50Fps: Double = 0.0,
    val p90Fps: Double = 0.0,
    val p95Fps: Double = 0.0,
    val p99Fps: Double = 0.0
)

data class ActionMetrics(
    val pageName: String,
    val actionName: String,
    val durationMs: Long,
    val memoryMb: Int,
    val cpuPercent: Double,
    val avgFrameTimeMs: Double,
    val jankPercentage: Double,
    val fps: Double,
    val platform: String,
    // Delta values (change during action)
    val deltaMemoryMb: Int = 0,
    val deltaCpuPercent: Double = 0.0,
    // Timestamps for trace correlation
    val startTimeMs: Long = 0,  // Absolute host time when action started
    val endTimeMs: Long = 0,  // Absolute host time when action ended
    val startTimeRelativeToTraceMs: Long = 0,  // Time relative to trace start (for querying trace segments)
    val endTimeRelativeToTraceMs: Long = 0,  // Time relative to trace start
    // Percentiles (populated from trace analysis or continuous sampling)
    val p50Cpu: Double = 0.0,
    val p90Cpu: Double = 0.0,
    val p95Cpu: Double = 0.0,
    val p99Cpu: Double = 0.0,
    val p50Memory: Double = 0.0,
    val p90Memory: Double = 0.0,
    val p95Memory: Double = 0.0,
    val p99Memory: Double = 0.0,
    val p50Fps: Double = 0.0,
    val p90Fps: Double = 0.0,
    val p95Fps: Double = 0.0,
    val p99Fps: Double = 0.0
)

data class PageMetricsSummary(
    val pageName: String,
    val actionCount: Int,
    val totalDurationMs: Long,
    val avgDurationMs: Double,
    val avgMemoryMb: Double,
    val peakMemoryMb: Int,
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgFrameTimeMs: Double,
    val avgJankPercentage: Double,
    val avgFps: Double,
    val actions: List<ActionMetrics>
)

data class TotalMetricsSummary(
    val appLaunchTimeMs: Long,
    val totalActions: Int,
    val totalDurationMs: Long,
    val avgActionDurationMs: Double,
    val avgMemoryMb: Double,
    val peakMemoryMb: Int,
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgFrameTimeMs: Double,
    val avgJankPercentage: Double,
    val avgFps: Double,
    val platform: String,
    val pageCount: Int,
    val pageSummaries: List<PageMetricsSummary>,
    // CPU Percentiles
    val p90CpuPercent: Double = 0.0,
    val p95CpuPercent: Double = 0.0,
    val p99CpuPercent: Double = 0.0,
    // Memory Percentiles
    val p90MemoryMb: Double = 0.0,
    val p95MemoryMb: Double = 0.0,
    val p99MemoryMb: Double = 0.0,
    // FPS Percentiles
    val p90Fps: Double = 0.0,
    val p95Fps: Double = 0.0,
    val p99Fps: Double = 0.0
)

