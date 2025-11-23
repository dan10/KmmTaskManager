package com.danioliveira.appium.metrics

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.metrics.android.CpuMetrics
import com.danioliveira.appium.metrics.android.MemInfoMetrics
import com.danioliveira.appium.metrics.android.TraceMetrics
import kotlinx.coroutines.*
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Improved Metrics Manager that uses continuous background sampling
 * and trace-based analysis for more accurate performance data.
 */
class ImprovedMetricsManager(
    private val platform: Platform,
    private val packageOrBundleId: String,
    private val androidCollector: AndroidMetricsCollector?
) : MetricsManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val samplingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Store samples for the current action
    private val currentCpuSamples = ConcurrentLinkedQueue<Sample<Double>>()
    private val currentMemorySamples = ConcurrentLinkedQueue<Sample<Int>>()
    
    // Store collected metrics
    private val actionMetrics = mutableListOf<ActionMetrics>()
    private var appLaunchTimeMs: Long = 0
    private var traceStartTimeMs: Long = 0
    private var traceMetrics: TraceMetrics? = null
    
    private val isSampling = AtomicBoolean(false)
    
    data class Sample<T>(val timestamp: Long, val value: T)
    
    /**
     * Start collecting metrics for an action.
     * Starts background sampling and tracing.
     */
    fun startActionCollection(actionName: String) {
        logger.debug("Starting collection for $actionName")
        
        // Inject trace marker for Android
        if (platform == Platform.ANDROID) {
            // Use Async markers (S) to handle separate adb shell sessions
            // Format: S|pid|act:ActionName|cookie
            val pid = androidCollector?.getPid() ?: 0
            val cookie = actionName.hashCode()
            androidCollector?.injectTraceMarker("S|$pid|act:$actionName|$cookie")
        }
        
        // Clear previous samples
        currentCpuSamples.clear()
        currentMemorySamples.clear()
        
        // Start Background Sampling
        startSampling()
        
        // Reset gfxinfo to start fresh for this action
        if (platform == Platform.ANDROID) {
            androidCollector?.resetGfxInfo()
        }
    }
    
    /**
     * Stop collecting metrics and return the result.
     */
    fun stopActionCollection(pageName: String, actionName: String, startTimeMs: Long): ActionMetrics {
        logger.debug("Stopping collection for $actionName")
        
        // Inject trace marker end for Android
        if (platform == Platform.ANDROID) {
            // Use Async markers (F) to match the Start marker
            // Format: F|pid|act:ActionName|cookie
            val pid = androidCollector?.getPid() ?: 0
            val cookie = actionName.hashCode()
            androidCollector?.injectTraceMarker("F|$pid|act:$actionName|$cookie")
        }
        
        // Stop Sampling
        stopSampling()
        
        // Aggregate Samples
        val cpuStats = calculateStats(currentCpuSamples)
        val memStats = calculateStats(currentMemorySamples)
        
        val endTimeMs = System.currentTimeMillis()
        val durationMs = endTimeMs - startTimeMs
        
        // Calculate percentiles
        val cpuPercentiles = calculatePercentiles(currentCpuSamples.map { it.value })
        val memPercentiles = calculatePercentiles(currentMemorySamples.map { it.value.toDouble() })
        
        // Collect FPS from dumpsys (Android only)
        var gfxMetrics: com.danioliveira.appium.metrics.android.GfxInfoMetrics? = null
        if (platform == Platform.ANDROID) {
            gfxMetrics = androidCollector?.collectGfxInfo()
        }
        
        return ActionMetrics(
            pageName = pageName,
            actionName = actionName,
            durationMs = durationMs,
            memoryMb = memStats.average.toInt(),
            cpuPercent = cpuStats.average,
            avgFrameTimeMs = gfxMetrics?.avgFrameTimeMs ?: 0.0,
            jankPercentage = gfxMetrics?.jankPercentage ?: 0.0,
            fps = gfxMetrics?.fps ?: 0.0,
            platform = platform.name,
            // Store peak values as well
            deltaMemoryMb = (memStats.max - memStats.min).toInt(),
            deltaCpuPercent = cpuStats.max - cpuStats.min,
            // Timestamps for trace correlation
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            startTimeRelativeToTraceMs = startTimeMs - traceStartTimeMs,
            endTimeRelativeToTraceMs = endTimeMs - traceStartTimeMs,
            // Percentiles
            p50Cpu = cpuPercentiles.p50,
            p90Cpu = cpuPercentiles.p90,
            p95Cpu = cpuPercentiles.p95,
            p99Cpu = cpuPercentiles.p99,
            p50Memory = memPercentiles.p50,
            p90Memory = memPercentiles.p90,
            p95Memory = memPercentiles.p95,
            p99Memory = memPercentiles.p99
        )
    }
    
    private fun startSampling() {
        isSampling.set(true)
        samplingScope.launch {
            while (isSampling.get()) {
                val timestamp = System.currentTimeMillis()
                
                if (platform == Platform.ANDROID && androidCollector != null) {
                    // Collect CPU
                    // Note: dumpsys cpuinfo is lighter than top but still has overhead.
                    // Ideally read /proc/stat if possible, or use a very lightweight shell command.
                    val cpu = androidCollector.collectCpuInfoFromDumpsys()
                    currentCpuSamples.add(Sample(timestamp, cpu.cpuPercentage))
                    
                    // Collect Memory
                    val mem = androidCollector.collectMemInfo()
                    currentMemorySamples.add(Sample(timestamp, mem.rssMb))
                }
                
                // Sample every 200ms
                delay(200)
            }
        }
    }
    
    private fun stopSampling() {
        isSampling.set(false)
    }
    
    private data class Stats(
        val average: Double,
        val max: Double,
        val min: Double,
        val duration: Long
    )
    
    private fun <T : Number> calculateStats(samples: Collection<Sample<T>>): Stats {
        if (samples.isEmpty()) return Stats(0.0, 0.0, 0.0, 0)
        
        val values = samples.map { it.value.toDouble() }
        val timestamps = samples.map { it.timestamp }
        
        return Stats(
            average = values.average(),
            max = values.maxOrNull() ?: 0.0,
            min = values.minOrNull() ?: 0.0,
            duration = (timestamps.maxOrNull() ?: 0) - (timestamps.minOrNull() ?: 0)
        )
    }

    data class Percentiles(
        val p50: Double,
        val p90: Double,
        val p95: Double,
        val p99: Double
    )

    private fun calculatePercentiles(values: List<Double>): Percentiles {
        if (values.isEmpty()) return Percentiles(0.0, 0.0, 0.0, 0.0)
        
        val stats = DescriptiveStatistics()
        values.forEach { stats.addValue(it) }
        
        return Percentiles(
            p50 = stats.getPercentile(50.0),
            p90 = stats.getPercentile(90.0),
            p95 = stats.getPercentile(95.0),
            p99 = stats.getPercentile(99.0)
        )
    }
    override fun measureAppLaunchTime(): Long {
        appLaunchTimeMs = androidCollector?.measureLaunchTime() ?: 0L
        return appLaunchTimeMs
    }

    override fun getAppLaunchTime(): Long {
        return appLaunchTimeMs
    }

    override fun startPerformanceRecording() {
        traceStartTimeMs = System.currentTimeMillis()
        logger.info("ImprovedMetricsManager: Starting global performance recording")
        
        if (platform == Platform.ANDROID) {
            // Use Perfetto instead of Systrace
            // Increase buffer to 256MB to prevent truncation during long tests
            val config = com.danioliveira.appium.perf.core.PerformanceConfig(
                platform = platform,
                systraceBufferSizeKb = 262144 // 256MB
            )
            androidCollector?.perfettoCollector?.startCapture(
                config = config,
                packageName = packageOrBundleId
            )
        }
    }

    override fun stopPerformanceRecording(testName: String): String? {
        logger.info("ImprovedMetricsManager: Stopping global performance recording")
        
        if (platform == Platform.ANDROID) {
            val traceFile = androidCollector?.perfettoCollector?.stopCapture(testName)
            if (traceFile != null) {
                traceMetrics = androidCollector?.perfettoCollector?.parseTrace(traceFile)
                logger.info("✅ Global Perfetto trace captured: ${traceFile.name}")
                
                // Now that we have the trace, update all collected action metrics with trace data
                updateMetricsWithTraceData()
                
                return "Perfetto trace saved to: ${traceFile.absolutePath}"
            }
        }
        return null
    }
    
    private fun updateMetricsWithTraceData() {
        val trace = traceMetrics ?: return
        
        logger.info("Updating ${actionMetrics.size} actions with trace data...")
        
        actionMetrics.forEachIndexed { index, metric ->
            // Query trace for the specific time window of this action
            // Use the queryTraceSegment method directly from TraceMetrics
            val segment = trace.queryTraceSegment(
                metric.startTimeRelativeToTraceMs,
                metric.endTimeRelativeToTraceMs
            )
            
            if (segment != null) {
                actionMetrics[index] = metric.copy(
                    // Only overwrite if trace has valid data, otherwise keep dumpsys data
                    avgFrameTimeMs = if (segment.avgFps > 0) 1000.0 / segment.avgFps else metric.avgFrameTimeMs,
                    jankPercentage = if (segment.jankCount > 0) 100.0 else metric.jankPercentage,
                    fps = if (segment.avgFps > 0) segment.avgFps else metric.fps,
                    // Update CPU/Mem from trace as it's more accurate than polling
                    cpuPercent = segment.avgCpuPercent,
                    memoryMb = if (segment.avgMemoryMb > 0) segment.avgMemoryMb else metric.memoryMb,
                    // Update CPU percentiles from trace
                    p50Cpu = segment.p50Cpu,
                    p90Cpu = segment.p90Cpu,
                    p95Cpu = segment.p95Cpu,
                    p99Cpu = segment.p99Cpu
                )
            }
        }
    }

    override fun trackAction(pageName: String, actionName: String, block: () -> Unit) {
        val startTimeMs = System.currentTimeMillis()
        startActionCollection(actionName)
        try {
            block()
        } finally {
            val metrics = stopActionCollection(pageName, actionName, startTimeMs)
            actionMetrics.add(metrics)
        }
    }

    override fun getActionMetrics(): List<ActionMetrics> {
        return actionMetrics.toList()
    }

    override fun analyzeTraceSegments(): Map<String, TraceSegmentMetrics> {
        // In improved mode, we calculate metrics per action, so we can just aggregate them here
        val results = mutableMapOf<String, TraceSegmentMetrics>()
        
        val actionGroups = actionMetrics.groupBy { "${it.pageName}.${it.actionName}" }
        
        actionGroups.forEach { (actionKey, metricsList) ->
            val avgCpu = metricsList.map { it.cpuPercent }.average()
            val peakCpu = metricsList.maxOf { it.cpuPercent }
            val avgMem = metricsList.map { it.memoryMb }.average().toInt()
            val peakMem = metricsList.maxOf { it.memoryMb }
            val avgFps = metricsList.map { it.fps }.average()
            val minFps = metricsList.minOf { it.fps }
            
            results[actionKey] = TraceSegmentMetrics(
                avgCpuPercent = avgCpu,
                peakCpuPercent = peakCpu,
                avgMemoryMb = avgMem,
                peakMemoryMb = peakMem,
                avgFps = avgFps,
                minFps = minFps,
                jankCount = 0 // Jank calculation needs improvement in per-action mode
            )
        }
        return results
    }

    override fun reset() {
        actionMetrics.clear()
        currentCpuSamples.clear()
        currentMemorySamples.clear()
        appLaunchTimeMs = 0
        isSampling.set(false)
    }

    override fun getPageSummary(pageName: String): PageMetricsSummary? {
        val metrics = actionMetrics.filter { it.pageName == pageName }
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
            actions = metrics
        )
    }

    override fun getTotalSummary(): TotalMetricsSummary {
        val allMetrics = actionMetrics
        val pageNames = allMetrics.map { it.pageName }.distinct()
        val pageSummaries = pageNames.mapNotNull { getPageSummary(it) }
        
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
            pageCount = pageNames.size,
            pageSummaries = pageSummaries
        )
    }

    override fun getTraceMetrics(): TraceMetrics? {
        return traceMetrics
    }
}
