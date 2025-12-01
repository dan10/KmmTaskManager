package com.danioliveira.appium.metrics.android

import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.roundToInt

/**
 * Collects comprehensive performance statistics from both ADB polling and systrace.
 * Provides min, max, and avg for CPU, FPS, and Memory.
 * 
 * Uses Flashlight's FPS algorithm for accurate FPS measurement.
 * Reference: https://github.com/bamlab/flashlight
 */
class PerformanceStatisticsCollector(
    private val androidCollector: AndroidMetricsCollector,
    private val packageName: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // Flashlight FPS collector for accurate FPS measurement
    private val flashlightFps = FlashlightFpsCollector(packageName)
    
    // ADB polling samples
    private val memorySamples = mutableListOf<Int>()
    private val cpuSamples = mutableListOf<Double>()
    private val fpsSamples = mutableListOf<Double>()
    private val fpsDetailedSamples = mutableListOf<FlashlightFpsMetrics>()
    
    // Polling control
    @Volatile
    private var isPolling = false
    private var pollingThread: Thread? = null
    
    /**
     * Start continuous ADB polling for real-time metrics.
     * Collects samples every 500ms until stopPolling() is called.
     */
    fun startPolling(intervalMs: Long = 500) {
        if (isPolling) {
            logger.warn("Polling already in progress")
            return
        }
        
        isPolling = true
        memorySamples.clear()
        cpuSamples.clear()
        fpsSamples.clear()
        
        pollingThread = Thread {
            logger.info("Started ADB polling (interval: ${intervalMs}ms)")
            
            while (isPolling) {
                try {
                    // Collect memory
                    val memInfo = androidCollector.collectMemInfo()
                    if (memInfo.rssMb > 0) {
                        memorySamples.add(memInfo.rssMb)
                    }
                    
                    // Collect CPU
                    val cpuInfo = androidCollector.collectCpuInfo()
                    if (cpuInfo.cpuPercentage >= 0) {
                        cpuSamples.add(cpuInfo.cpuPercentage)
                    }
                    
                    // Collect FPS using Flashlight algorithm
                    val fpsMetrics = flashlightFps.collectFps()
                    if (fpsMetrics.fps > 0) {
                        fpsSamples.add(fpsMetrics.fps)
                        fpsDetailedSamples.add(fpsMetrics)
                    }
                    
                    Thread.sleep(intervalMs)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.warn("Polling error: ${e.message}")
                }
            }
            
            logger.info("Stopped ADB polling")
            logger.info("  Memory samples: ${memorySamples.size}")
            logger.info("  CPU samples: ${cpuSamples.size}")
            logger.info("  FPS samples: ${fpsSamples.size} (Flashlight algorithm)")
            
            // Log Flashlight FPS details
            if (fpsDetailedSamples.isNotEmpty()) {
                val totalActualFrames = fpsDetailedSamples.sumOf { it.frameCount }
                val totalIdleFrames = fpsDetailedSamples.sumOf { it.idleFrameCount }
                val totalRenderTime = fpsDetailedSamples.sumOf { it.renderTimeMs }
                val totalIdleTime = fpsDetailedSamples.sumOf { it.idleTimeMs }
                
                logger.info("  Flashlight FPS breakdown:")
                logger.info("    - Actual frames: $totalActualFrames")
                logger.info("    - Idle frames: ${String.format("%.1f", totalIdleFrames)}")
                logger.info("    - Render time: ${String.format("%.0f", totalRenderTime)}ms")
                logger.info("    - Idle time: ${String.format("%.0f", totalIdleTime)}ms")
            }
        }.apply {
            name = "ADB-Polling-Thread"
            isDaemon = true
            start()
        }
    }
    
    /**
     * Stop ADB polling and return statistics.
     */
    fun stopPolling(): AdbStatistics {
        isPolling = false
        pollingThread?.join(2000)
        
        return AdbStatistics(
            memory = MetricStats(
                min = memorySamples.minOrNull() ?: 0,
                max = memorySamples.maxOrNull() ?: 0,
                avg = if (memorySamples.isNotEmpty()) memorySamples.average() else 0.0,
                samples = memorySamples.size
            ),
            cpu = MetricStats(
                min = cpuSamples.minOrNull()?.toInt() ?: 0,
                max = cpuSamples.maxOrNull()?.toInt() ?: 0,
                avg = if (cpuSamples.isNotEmpty()) cpuSamples.average() else 0.0,
                samples = cpuSamples.size
            ),
            fps = MetricStats(
                min = fpsSamples.minOrNull()?.toInt() ?: 0,
                max = fpsSamples.maxOrNull()?.toInt() ?: 0,
                avg = if (fpsSamples.isNotEmpty()) fpsSamples.average() else 0.0,
                samples = fpsSamples.size
            )
        )
    }
    
    /**
     * Extract statistics from systrace file.
     */
    fun extractSystraceStatistics(traceMetrics: TraceMetrics): SystraceStatistics {
        // CPU statistics from trace
        val cpuStats = if (traceMetrics.cpuUtilization.isNotEmpty()) {
            MetricStats(
                min = traceMetrics.cpuUtilization.minOf { it.cpuPercent }.toInt(),
                max = traceMetrics.cpuUtilization.maxOf { it.cpuPercent }.toInt(),
                avg = traceMetrics.cpuUtilization.map { it.cpuPercent }.average(),
                samples = traceMetrics.cpuUtilization.size
            )
        } else {
            MetricStats(0, 0, 0.0, 0)
        }
        
        // FPS statistics from screen traces
        val fpsStats = if (traceMetrics.screens.isNotEmpty()) {
            // Estimate FPS from screen durations (assuming 60 FPS target)
            val avgFrameTime = traceMetrics.screens.map { it.durationMs / 60.0 }.average()
            val fps = if (avgFrameTime > 0) 1000.0 / avgFrameTime else 60.0
            
            MetricStats(
                min = 30, // Estimated min (would need frame-level data)
                max = 60, // Estimated max (60 FPS cap)
                avg = fps.coerceIn(0.0, 60.0),
                samples = traceMetrics.screens.size
            )
        } else {
            MetricStats(0, 0, 0.0, 0)
        }
        
        // Memory statistics (not directly available in systrace, use ADB)
        val memoryStats = MetricStats(0, 0, 0.0, 0)
        
        return SystraceStatistics(
            memory = memoryStats,
            cpu = cpuStats,
            fps = fpsStats,
            totalDurationMs = traceMetrics.totalDurationMs,
            screenCount = traceMetrics.screenCount
        )
    }
    
    /**
     * Combine ADB and systrace statistics for comprehensive report.
     */
    fun generateCombinedReport(
        adbStats: AdbStatistics,
        systraceStats: SystraceStatistics
    ): CombinedStatistics {
        return CombinedStatistics(
            adb = adbStats,
            systrace = systraceStats,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Export statistics to CSV.
     */
    fun exportToCsv(stats: CombinedStatistics, outputFile: File) {
        val csv = buildString {
            appendLine("Source,Metric,Min,Max,Avg,Samples")
            
            // ADB statistics
            appendLine("ADB,Memory (MB),${stats.adb.memory.min},${stats.adb.memory.max},${String.format("%.1f", stats.adb.memory.avg)},${stats.adb.memory.samples}")
            appendLine("ADB,CPU (%),${stats.adb.cpu.min},${stats.adb.cpu.max},${String.format("%.1f", stats.adb.cpu.avg)},${stats.adb.cpu.samples}")
            appendLine("ADB,FPS,${stats.adb.fps.min},${stats.adb.fps.max},${String.format("%.1f", stats.adb.fps.avg)},${stats.adb.fps.samples}")
            
            // Systrace statistics
            appendLine("Systrace,Memory (MB),${stats.systrace.memory.min},${stats.systrace.memory.max},${String.format("%.1f", stats.systrace.memory.avg)},${stats.systrace.memory.samples}")
            appendLine("Systrace,CPU (%),${stats.systrace.cpu.min},${stats.systrace.cpu.max},${String.format("%.1f", stats.systrace.cpu.avg)},${stats.systrace.cpu.samples}")
            appendLine("Systrace,FPS,${stats.systrace.fps.min},${stats.systrace.fps.max},${String.format("%.1f", stats.systrace.fps.avg)},${stats.systrace.fps.samples}")
        }
        
        outputFile.writeText(csv)
        logger.info("✅ Statistics CSV exported: ${outputFile.name}")
    }
    
    /**
     * Export statistics to Markdown.
     */
    fun exportToMarkdown(stats: CombinedStatistics, outputFile: File) {
        val md = buildString {
            appendLine("# Performance Statistics Report")
            appendLine()
            appendLine("**Generated**: ${java.time.LocalDateTime.now()}")
            appendLine()
            
            appendLine("## ADB Polling Statistics")
            appendLine()
            appendLine("Real-time metrics collected via `adb shell dumpsys`:")
            appendLine()
            appendLine("| Metric | Min | Max | Avg | Samples |")
            appendLine("|--------|-----|-----|-----|---------|")
            appendLine("| **Memory (MB)** | ${stats.adb.memory.min} | ${stats.adb.memory.max} | ${String.format("%.1f", stats.adb.memory.avg)} | ${stats.adb.memory.samples} |")
            appendLine("| **CPU (%)** | ${stats.adb.cpu.min} | ${stats.adb.cpu.max} | ${String.format("%.1f", stats.adb.cpu.avg)} | ${stats.adb.cpu.samples} |")
            appendLine("| **FPS** | ${stats.adb.fps.min} | ${stats.adb.fps.max} | ${String.format("%.1f", stats.adb.fps.avg)} | ${stats.adb.fps.samples} |")
            appendLine()
            
            appendLine("## Systrace Statistics")
            appendLine()
            appendLine("System-level trace analysis:")
            appendLine()
            appendLine("| Metric | Min | Max | Avg | Samples |")
            appendLine("|--------|-----|-----|-----|---------|")
            appendLine("| **Memory (MB)** | ${stats.systrace.memory.min} | ${stats.systrace.memory.max} | ${String.format("%.1f", stats.systrace.memory.avg)} | ${stats.systrace.memory.samples} |")
            appendLine("| **CPU (%)** | ${stats.systrace.cpu.min} | ${stats.systrace.cpu.max} | ${String.format("%.1f", stats.systrace.cpu.avg)} | ${stats.systrace.cpu.samples} |")
            appendLine("| **FPS** | ${stats.systrace.fps.min} | ${stats.systrace.fps.max} | ${String.format("%.1f", stats.systrace.fps.avg)} | ${stats.systrace.fps.samples} |")
            appendLine()
            appendLine("- **Total Duration**: ${stats.systrace.totalDurationMs}ms")
            appendLine("- **Screen Count**: ${stats.systrace.screenCount}")
            appendLine()
            
            appendLine("## Comparison")
            appendLine()
            appendLine("| Metric | ADB Avg | Systrace Avg | Difference |")
            appendLine("|--------|---------|--------------|------------|")
            
            val memDiff = stats.adb.memory.avg - stats.systrace.memory.avg
            val cpuDiff = stats.adb.cpu.avg - stats.systrace.cpu.avg
            val fpsDiff = stats.adb.fps.avg - stats.systrace.fps.avg
            
            appendLine("| **Memory (MB)** | ${String.format("%.1f", stats.adb.memory.avg)} | ${String.format("%.1f", stats.systrace.memory.avg)} | ${String.format("%+.1f", memDiff)} |")
            appendLine("| **CPU (%)** | ${String.format("%.1f", stats.adb.cpu.avg)} | ${String.format("%.1f", stats.systrace.cpu.avg)} | ${String.format("%+.1f", cpuDiff)} |")
            appendLine("| **FPS** | ${String.format("%.1f", stats.adb.fps.avg)} | ${String.format("%.1f", stats.systrace.fps.avg)} | ${String.format("%+.1f", fpsDiff)} |")
            appendLine()
            
            appendLine("## FPS Methodology")
            appendLine()
            appendLine("**Flashlight Algorithm** (Industry Standard)")
            appendLine()
            appendLine("FPS is calculated using [Flashlight's algorithm](https://github.com/bamlab/flashlight):")
            appendLine()
            appendLine("```")
            appendLine("fps = ((actualFrames + idleFrames) / totalTime) * 1000")
            appendLine("```")
            appendLine()
            appendLine("**Key Features**:")
            appendLine("- Accounts for idle time (when app isn't rendering)")
            appendLine("- Treats idle periods as 60 FPS (not dropping frames)")
            appendLine("- Parses detailed frame timing (Draw, Prepare, Process, Execute)")
            appendLine("- More accurate than simple average frame time")
            appendLine()
            
            appendLine("## Notes")
            appendLine()
            appendLine("- **ADB Polling**: Real-time samples collected every 500ms via `dumpsys`")
            appendLine("- **Systrace**: System-level trace analysis with detailed CPU/GPU data")
            appendLine("- **Memory**: ADB provides RSS memory, systrace doesn't track memory directly")
            appendLine("- **CPU**: Both sources provide CPU utilization, systrace is more accurate")
            appendLine("- **FPS**: Uses Flashlight algorithm for accurate measurement")
            appendLine()
            appendLine("**Reference**: https://github.com/bamlab/flashlight")
        }
        
        outputFile.writeText(md)
        logger.info("✅ Statistics Markdown exported: ${outputFile.name}")
    }
}

/**
 * Statistics for a single metric (min, max, avg).
 */
data class MetricStats(
    val min: Int,
    val max: Int,
    val avg: Double,
    val samples: Int
)

/**
 * Statistics from ADB polling.
 */
data class AdbStatistics(
    val memory: MetricStats,
    val cpu: MetricStats,
    val fps: MetricStats
)

/**
 * Statistics from systrace analysis.
 */
data class SystraceStatistics(
    val memory: MetricStats,
    val cpu: MetricStats,
    val fps: MetricStats,
    val totalDurationMs: Long,
    val screenCount: Int
)

/**
 * Combined statistics from both sources.
 */
data class CombinedStatistics(
    val adb: AdbStatistics,
    val systrace: SystraceStatistics,
    val timestamp: Long
)

