package com.danioliveira.appium.metrics.android

import java.io.File

/**
 * Complete trace metrics from a trace capture (Systrace or Perfetto).
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
    val fpsPerSecond: List<FpsPerSecond> = emptyList(),  // Per-second FPS time series
    val memoryUsage: List<MemoryUsage> = emptyList(), // Memory usage over time
    val traceStartTs: Long? = null // Absolute start timestamp of the trace (in ms)
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
    
    /**
     * Query trace metrics for a specific time window.
     * Filters CPU, FPS, and other metrics within the given time range.
     * 
     * @param startMs Start time relative to trace start (milliseconds)
     * @param endMs End time relative to trace start (milliseconds)
     * @return Metrics for the specified time window, or null if no data
     */
    fun queryTraceSegment(startMs: Long, endMs: Long): com.danioliveira.appium.metrics.TraceSegmentMetrics? {
        if (cpuUtilization.isEmpty()) {
            return null
        }
        
        // Find the trace's actual start time
        // Use explicit traceStartTs if available (from Perfetto bounds), otherwise fallback to first CPU data point
        val traceActualStart = traceStartTs ?: cpuUtilization.minOf { it.timestampMs }
        
        // Convert relative times to absolute trace times
        val absoluteStart = traceActualStart + startMs
        val absoluteEnd = traceActualStart + endMs
        
        // Filter CPU data within the time window
        // Note: Perfetto timestamps are in milliseconds (converted from ns in PerfettoMetricsExtractor)
        // But we need to be careful about relative vs absolute time
        val cpuInWindow = cpuUtilization.filter { 
            it.timestampMs >= absoluteStart && it.timestampMs <= absoluteEnd 
        }
        
        if (cpuInWindow.isEmpty()) {
            return null
        }
        
        // Calculate CPU metrics
        val cpuValues = cpuInWindow.map { it.cpuPercent }.sorted()
        val avgCpu = cpuValues.average()
        val peakCpu = cpuValues.maxOrNull() ?: 0.0
        
        // Calculate percentiles
        fun getPercentile(values: List<Double>, percentile: Double): Double {
            if (values.isEmpty()) return 0.0
            val index = kotlin.math.ceil(percentile * values.size).toInt() - 1
            return values[index.coerceIn(0, values.lastIndex)]
        }
        
        val p50Cpu = getPercentile(cpuValues, 0.50)
        val p90Cpu = getPercentile(cpuValues, 0.90)
        val p95Cpu = getPercentile(cpuValues, 0.95)
        val p99Cpu = getPercentile(cpuValues, 0.99)
        
        // Calculate Memory metrics
        val memoryInWindow = memoryUsage.filter {
            it.timestampMs >= absoluteStart && it.timestampMs <= absoluteEnd
        }
        
        val avgMemoryMb = if (memoryInWindow.isNotEmpty()) {
            memoryInWindow.map { it.rssMb }.average().toInt()
        } else 0
        
        val peakMemoryMb = if (memoryInWindow.isNotEmpty()) {
            memoryInWindow.maxOf { it.rssMb }
        } else 0
        
        // Calculate FPS metrics
        // fpsPerSecond stores timestamp in seconds
        val startSec = absoluteStart / 1000
        val endSec = absoluteEnd / 1000
        
        val fpsInWindow = fpsPerSecond.filter {
            it.second >= startSec && it.second <= endSec
        }
        
        val avgFps = if (fpsInWindow.isNotEmpty()) {
            fpsInWindow.map { it.fps }.average()
        } else 0.0
        
        val minFps = if (fpsInWindow.isNotEmpty()) {
            fpsInWindow.minOf { it.fps }.toDouble()
        } else 0.0
        
        val jankCount = 0 // Jank count per segment requires frame-level analysis which is complex here
        
        return com.danioliveira.appium.metrics.TraceSegmentMetrics(
            avgCpuPercent = avgCpu,
            peakCpuPercent = peakCpu,
            avgMemoryMb = avgMemoryMb,
            peakMemoryMb = peakMemoryMb,
            avgFps = avgFps,
            minFps = minFps,
            jankCount = jankCount,
            p50Cpu = p50Cpu,
            p90Cpu = p90Cpu,
            p95Cpu = p95Cpu,
            p99Cpu = p99Cpu
        )
    }
    
    companion object {
        fun empty() = TraceMetrics(null, emptyList(), 0L, emptyList(), null)
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
    val core: Int? = null
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
    val second: Long,
    val fps: Int
)

/**
 * Memory usage data point.
 */
data class MemoryUsage(
    val timestampNs: Long,
    val rssKb: Long
) {
    val timestampMs: Long get() = timestampNs / 1_000_000
    val rssMb: Int get() = (rssKb / 1024).toInt()
}
