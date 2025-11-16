package com.danioliveira.appium.metrics.android.perfetto

import androidx.benchmark.traceprocessor.ExperimentalTraceProcessorApi
import androidx.benchmark.traceprocessor.PerfettoTrace
import androidx.benchmark.traceprocessor.TraceProcessor
import com.danioliveira.appium.metrics.android.TraceMetrics
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Extracts performance metrics from Perfetto traces using SQL queries.
 * 
 * This class provides high-level methods to extract common metrics:
 * - Startup timing
 * - Frame timing and jank
 * - Memory usage
 * - CPU utilization
 * - Custom trace sections
 * 
 * It serves as a bridge between the SQL queries and the existing TraceMetrics data structure.
 * 
 * Uses the official androidx.benchmark.traceprocessor.TraceProcessor library.
 */
@OptIn(ExperimentalTraceProcessorApi::class)
class PerfettoMetricsExtractor(private val traceFile: File) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val lifecycleManager = DesktopServerLifecycleManager()
    private var serverHandle: TraceProcessor.Handle? = null
    
    /**
     * Extract all metrics for a package.
     * 
     * @param packageName App package name
     * @param startMs Start time in milliseconds (relative to trace start)
     * @param endMs End time in milliseconds (relative to trace start)
     * @return TraceMetrics compatible with existing framework
     */
    fun extractMetrics(
        packageName: String,
        startMs: Long? = null,
        endMs: Long? = null
    ): TraceMetrics {
        logger.info("Extracting metrics for $packageName")
        
        // Convert ms to ns for SQL queries
        val startNs = startMs?.let { it * 1_000_000 }
        val endNs = endMs?.let { it * 1_000_000 }
        
        // Extract individual metrics
        val startup = extractStartupTiming(packageName)
        val frames = extractFrameTiming(packageName, startNs, endNs)
        val fpsPerSecondData = extractFpsPerSecond(packageName, startNs, endNs)
        val memory = extractMemoryUsage(packageName, startNs, endNs)
        val cpu = extractCpuUtilization(packageName, startNs, endNs)
        val sections = extractTraceSections("act:", startNs, endNs)
        
        // Calculate aggregate metrics for frames
        val jankCount = frames.count { it.isJank }
        val totalFrames = frames.size
        val avgFrameTimeMs = if (frames.isNotEmpty()) {
            frames.map { it.durationMs }.average()
        } else 0.0
        
        // Calculate memory statistics (min/max/avg/p50/p90)
        val memoryMbValues = memory.map { it.rssMb.toDouble() }
        val minMemoryMb = memoryMbValues.minOrNull()?.toInt() ?: 0
        val maxMemoryMb = memoryMbValues.maxOrNull()?.toInt() ?: 0
        val avgMemoryMb = if (memoryMbValues.isNotEmpty()) {
            memoryMbValues.average().toInt()
        } else 0
        val p50MemoryMb = if (memoryMbValues.isNotEmpty()) {
            calculatePercentile(memoryMbValues, 0.50).toInt()
        } else 0
        val p90MemoryMb = if (memoryMbValues.isNotEmpty()) {
            calculatePercentile(memoryMbValues, 0.90).toInt()
        } else 0
        
        // Calculate CPU statistics by bucketing into time windows
        val cpuPercentages = bucketizeCpuToPercentages(cpu, startNs, endNs)
        val minCpuPercent = cpuPercentages.minOrNull() ?: 0.0
        val maxCpuPercent = cpuPercentages.maxOrNull() ?: 0.0
        val avgCpuPercent = if (cpuPercentages.isNotEmpty()) {
            cpuPercentages.average()
        } else 0.0
        
        // Calculate FPS statistics from frame durations
        val fpsValues = calculateFpsFromFrames(frames)
        val minFps = fpsValues.minOrNull() ?: 0.0
        val maxFps = fpsValues.maxOrNull() ?: 0.0
        val avgFps = if (fpsValues.isNotEmpty()) {
            fpsValues.average()
        } else 0.0
        
        logger.info("Extracted metrics:")
        logger.info("  Startup: ${startup?.durationMs}ms")
        logger.info("  Frames: $totalFrames (${jankCount} janky)")
        if (frames.isNotEmpty()) {
            logger.info("  Frame source: ${determineFrameSource(frames)}")
        }
        logger.info("  Avg frame time: ${String.format("%.2f", avgFrameTimeMs)}ms")
        logger.info("  Memory: min=${minMemoryMb}MB, max=${maxMemoryMb}MB, avg=${avgMemoryMb}MB, p50=${p50MemoryMb}MB, p90=${p90MemoryMb}MB (${memoryMbValues.size} samples)")
        logger.info("  CPU: min=${String.format("%.1f", minCpuPercent)}%, max=${String.format("%.1f", maxCpuPercent)}%, avg=${String.format("%.1f", avgCpuPercent)}%")
        logger.info("  FPS: min=${String.format("%.1f", minFps)}, max=${String.format("%.1f", maxFps)}, avg=${String.format("%.1f", avgFps)}")
        logger.info("  Trace sections: ${sections.size}")
        
        // Convert CPU slices to time-bucketed percentages for CpuUtilization
        val cpuUtilizationList = convertCpuToCpuUtilization(cpu, cpuPercentages, startNs, endNs)
        
        // Calculate per-screen metrics
        val screenMetricsList = sections.map { section ->
            calculateScreenMetrics(
                section = section,
                cpu = cpu,
                memory = memory,
                frames = frames,
                packageName = packageName
            )
        }
        
        // Convert per-second FPS data to FpsPerSecond objects
        val fpsPerSecondList = fpsPerSecondData.map { (second, fps) ->
            com.danioliveira.appium.metrics.android.FpsPerSecond(
                second = second,
                fps = fps
            )
        }
        
        logger.info("Per-second FPS: ${fpsPerSecondList.size} data points")
        if (fpsPerSecondList.isNotEmpty()) {
            val avgFpsPerSecond = fpsPerSecondList.map { it.fps }.average()
            val maxFpsPerSecond = fpsPerSecondList.maxOfOrNull { it.fps } ?: 0
            val minFpsPerSecond = fpsPerSecondList.minOfOrNull { it.fps } ?: 0
            logger.info("  FPS range: min=$minFpsPerSecond, max=$maxFpsPerSecond, avg=${String.format("%.1f", avgFpsPerSecond)}")
        }
        
        // Convert to existing TraceMetrics format
        return TraceMetrics(
            traceFile = traceFile,
            screens = sections.map { section ->
                com.danioliveira.appium.metrics.android.ScreenTrace(
                    name = section.name.removePrefix("act:"),
                    startTimeMs = section.timestampMs,
                    endTimeMs = section.timestampMs + section.durationMs.toLong(),
                    durationMs = section.durationMs.toLong()
                )
            },
            totalDurationMs = calculateTotalDuration(),
            cpuUtilization = cpuUtilizationList,
            startupTimeMs = startup?.durationMs?.toLong(),
            fps = avgFps,
            frameCount = totalFrames,
            screenMetrics = screenMetricsList,
            fpsPerSecond = fpsPerSecondList
        )
    }
    
    /**
     * Calculate performance metrics for a specific screen section.
     */
    private fun calculateScreenMetrics(
        section: TraceSection,
        cpu: List<CpuUtilization>,
        memory: List<MemoryUsage>,
        frames: List<FrameTiming>,
        packageName: String
    ): com.danioliveira.appium.metrics.android.ScreenMetrics {
        val sectionStartNs = section.timestampNs
        val sectionEndNs = section.timestampNs + section.durationNs
        
        // Filter data to this screen's time range
        val screenCpu = cpu.filter { it.timestampNs in sectionStartNs..sectionEndNs }
        val screenMemory = memory.filter { it.timestampNs in sectionStartNs..sectionEndNs }
        val screenFrames = frames.filter { it.timestampNs in sectionStartNs..sectionEndNs }
        
        // Calculate CPU stats
        val cpuPercentages = if (screenCpu.isNotEmpty()) {
            bucketizeCpuToPercentages(screenCpu, sectionStartNs, sectionEndNs)
        } else emptyList()
        
        val cpuMin = cpuPercentages.minOrNull() ?: 0.0
        val cpuMax = cpuPercentages.maxOrNull() ?: 0.0
        val cpuAvg = if (cpuPercentages.isNotEmpty()) cpuPercentages.average() else 0.0
        
        // Calculate Memory stats
        val memoryMbValues = screenMemory.map { it.rssMb.toDouble() }
        val memoryMin = memoryMbValues.minOrNull()?.toInt() ?: 0
        val memoryMax = memoryMbValues.maxOrNull()?.toInt() ?: 0
        val memoryAvg = if (memoryMbValues.isNotEmpty()) memoryMbValues.average().toInt() else 0
        val memoryP50 = if (memoryMbValues.isNotEmpty()) calculatePercentile(memoryMbValues, 0.50).toInt() else 0
        val memoryP90 = if (memoryMbValues.isNotEmpty()) calculatePercentile(memoryMbValues, 0.90).toInt() else 0
        
        // Calculate FPS stats
        val fpsValues = if (screenFrames.isNotEmpty()) {
            calculateFpsFromFrames(screenFrames)
        } else emptyList()
        
        val fpsMin = fpsValues.minOrNull() ?: 0.0
        val fpsMax = fpsValues.maxOrNull() ?: 0.0
        val fpsAvg = if (fpsValues.isNotEmpty()) fpsValues.average() else 0.0
        
        val jankCount = screenFrames.count { it.isJank }
        
        logger.info("Screen '${section.name}' metrics:")
        logger.info("  Duration: ${section.durationMs}ms")
        logger.info("  CPU: min=${String.format("%.1f", cpuMin)}%, max=${String.format("%.1f", cpuMax)}%, avg=${String.format("%.1f", cpuAvg)}% (${screenCpu.size} samples)")
        logger.info("  Memory: min=${memoryMin}MB, max=${memoryMax}MB, avg=${memoryAvg}MB, p50=${memoryP50}MB, p90=${memoryP90}MB (${screenMemory.size} samples)")
        logger.info("  FPS: min=${String.format("%.1f", fpsMin)}, max=${String.format("%.1f", fpsMax)}, avg=${String.format("%.1f", fpsAvg)} (${screenFrames.size} frames, $jankCount janky)")
        
        return com.danioliveira.appium.metrics.android.ScreenMetrics(
            screenName = section.name.removePrefix("act:"),
            durationMs = section.durationMs.toLong(),
            startMs = section.timestampMs,
            endMs = section.timestampMs + section.durationMs.toLong(),
            cpuMin = cpuMin,
            cpuMax = cpuMax,
            cpuAvg = cpuAvg,
            cpuSamples = screenCpu.size,
            memoryMin = memoryMin,
            memoryMax = memoryMax,
            memoryAvg = memoryAvg,
            memoryP50 = memoryP50,
            memoryP90 = memoryP90,
            memorySamples = screenMemory.size,
            fpsMin = fpsMin,
            fpsMax = fpsMax,
            fpsAvg = fpsAvg,
            frameCount = screenFrames.size,
            jankCount = jankCount
        )
    }
    
    /**
     * Calculate percentile from a list of values.
     * @param values List of numeric values
     * @param percentile Percentile to calculate (0.0 to 1.0, e.g., 0.50 for p50, 0.90 for p90)
     * @return The percentile value
     */
    private fun calculatePercentile(values: List<Double>, percentile: Double): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val index = (percentile * (sorted.size - 1)).toInt()
        return sorted[index]
    }
    
    /**
     * Bucketize CPU scheduling slices into time windows and calculate CPU percentage per window.
     * This converts raw sched slices into a time series of CPU utilization percentages.
     */
    private fun bucketizeCpuToPercentages(
        cpu: List<CpuUtilization>,
        startNs: Long?,
        endNs: Long?
    ): List<Double> {
        if (cpu.isEmpty()) return emptyList()
        
        val windowSizeMs = 100L // 100ms windows
        val windowSizeNs = windowSizeMs * 1_000_000
        
        val traceStartNs = startNs ?: cpu.minOf { it.timestampNs }
        val traceEndNs = endNs ?: cpu.maxOf { it.timestampNs + it.durationNs }
        
        // Group slices by time window
        val windowMap = mutableMapOf<Long, Long>() // window -> total CPU time in ns
        
        cpu.forEach { slice ->
            val windowIndex = (slice.timestampNs - traceStartNs) / windowSizeNs
            val cpuTimeInWindow = slice.durationNs
            windowMap[windowIndex] = (windowMap[windowIndex] ?: 0) + cpuTimeInWindow
        }
        
        // Convert to percentages
        return windowMap.values.map { cpuTimeNs ->
            val percentage = (cpuTimeNs.toDouble() / windowSizeNs) * 100.0
            percentage.coerceIn(0.0, 100.0)
        }
    }
    
    /**
     * Calculate FPS values from frame timing data.
     * Groups frames into time windows and calculates FPS per window.
     */
    private fun calculateFpsFromFrames(frames: List<FrameTiming>): List<Double> {
        if (frames.isEmpty()) return emptyList()
        
        val windowSizeMs = 1000L // 1 second windows for FPS calculation
        val windowSizeNs = windowSizeMs * 1_000_000
        
        val traceStartNs = frames.minOf { it.timestampNs }
        
        // Group frames by time window
        val windowMap = mutableMapOf<Long, MutableList<FrameTiming>>()
        
        frames.forEach { frame ->
            val windowIndex = (frame.timestampNs - traceStartNs) / windowSizeNs
            windowMap.getOrPut(windowIndex) { mutableListOf() }.add(frame)
        }
        
        // Calculate FPS per window
        return windowMap.values.map { windowFrames ->
            windowFrames.size.toDouble() // frames per second
        }
    }
    
    /**
     * Convert CPU slices to CpuUtilization time series with bucketed percentages.
     */
    private fun convertCpuToCpuUtilization(
        cpu: List<CpuUtilization>,
        cpuPercentages: List<Double>,
        startNs: Long?,
        endNs: Long?
    ): List<com.danioliveira.appium.metrics.android.CpuUtilization> {
        if (cpu.isEmpty() || cpuPercentages.isEmpty()) return emptyList()
        
        val windowSizeMs = 100L // 100ms windows (same as bucketizeCpuToPercentages)
        val windowSizeNs = windowSizeMs * 1_000_000
        
        val traceStartNs = startNs ?: cpu.minOf { it.timestampNs }
        
        // Create time series from bucketed percentages
        return cpuPercentages.mapIndexed { index, percentage ->
            val timestampNs = traceStartNs + (index * windowSizeNs)
            com.danioliveira.appium.metrics.android.CpuUtilization(
                timestampMs = timestampNs / 1_000_000,
                cpuPercent = percentage,
                core = null
            )
        }
    }
    
    /**
     * Execute a SQL query using the TraceProcessor.
     */
    private fun <T> executeQuery(sql: String, transform: (Map<String, Any?>) -> T?): List<T> {
        return try {
            // Start server if not already started
            if (serverHandle == null) {
                serverHandle = TraceProcessor.startServer(
                    lifecycleManager,
                    eventCallback = object : TraceProcessor.EventCallback {
                        override fun onLoadTraceFailure(trace: androidx.benchmark.traceprocessor.PerfettoTrace, throwable: Throwable) {
                            logger.error("Failed to load trace ${trace.path}: ${throwable.message}", throwable)
                        }
                    },
                    tracer = object : TraceProcessor.Tracer() {
                        override fun beginTraceSection(label: String) {
                            // No-op for desktop environment
                        }
                        override fun endTraceSection() {
                            // No-op for desktop environment
                        }
                    }
                )
            }
            
            val processor = serverHandle!!.traceProcessor
            
            // Load trace and execute query
            val results: List<T> = processor.loadTrace(
                trace = PerfettoTrace(traceFile.absolutePath),
                block = fun(session: TraceProcessor.Session): List<T> {
                    logger.debug("Executing SQL query (${sql.length} chars): ${sql.take(100)}...")
                    val queryResult = session.query(sql)
                    
                    // Convert query result to list of transformed objects
                    val resultList = mutableListOf<T>()
                    queryResult.forEach { row ->
                        // Row already implements Map<String, Any?>, so we can use it directly
                        transform(row)?.let { resultList.add(it) }
                    }
                    return resultList
                }
            )
            return results
        } catch (e: Exception) {
            logger.error("Query failed: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Extract startup timing.
     */
    fun extractStartupTiming(packageName: String): StartupTiming? {
        return try {
            val sql = PerfettoQueries.startupTimingQuery(packageName)
            val results = executeQuery(sql) { row ->
                StartupTiming(
                    type = row["startup_type"] as? String ?: "unknown",
                    durationMs = (row["duration_ms"] as? Number)?.toDouble() ?: 0.0,
                    startMs = (row["start_ms"] as? Number)?.toDouble() ?: 0.0
                )
            }
            results.firstOrNull()
        } catch (e: Exception) {
            logger.warn("Failed to extract startup timing: ${e.message}")
            null
        }
    }
    
    /**
     * Extract jank statistics from frame timing.
     */
    fun extractJankStats(
        packageName: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): JankStats {
        val frames = extractFrameTiming(packageName, startNs, endNs)
        return JankStats.from(frames)
    }
    
    /**
     * Extract FPS aggregated per second.
     * This provides a time-series view of FPS over the trace duration.
     * 
     * Returns a list of (second, fps) pairs where second is the timestamp in seconds
     * and fps is the number of frames rendered in that second.
     */
    fun extractFpsPerSecond(
        packageName: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<Pair<Long, Int>> {
        return try {
            // Try actual_frame_timeline_event first (Android 12+)
            val sql = PerfettoQueries.fpsPerSecondQuery(packageName, startNs, endNs)
            val results = executeQuery(sql) { row ->
                try {
                    val second = (row["second"] as? Number)?.toLong() ?: 0
                    val fps = (row["fps"] as? Number)?.toInt() ?: 0
                    second to fps
                } catch (e: Exception) {
                    logger.debug("Skipping invalid FPS row: ${e.message}")
                    null
                }
            }
            
            if (results.isNotEmpty()) {
                return results
            }
            
            // Fallback: Calculate per-second FPS from Choreographer frames
            logger.debug("No frame_timeline_event data, calculating per-second FPS from Choreographer")
            val frames = extractFrameTiming(packageName, startNs, endNs)
            
            if (frames.isEmpty()) {
                return emptyList()
            }
            
            // Group frames by second
            val framesBySecond = frames.groupBy { frame ->
                (frame.timestampNs + frame.durationNs) / 1_000_000_000
            }
            
            // Convert to list of (second, fps) pairs
            framesBySecond.map { (second, framesInSecond) ->
                second to framesInSecond.size
            }.sortedBy { it.first }
            
        } catch (e: Exception) {
            logger.debug("Failed to extract per-second FPS: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract frame timing with proper fallback chain for different Android versions.
     * Priority: frame_timeline_event (Android 12+) > traced sections > frame_timeline_slice > Choreographer
     */
    fun extractFrameTiming(
        packageName: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<FrameTiming> {
        return try {
            // Priority 1: Try actual_frame_timeline_event (Android 12+)
            logger.debug("Attempting to extract frames from actual_frame_timeline_event (Android 12+)")
            val eventFrames = extractFrameTimelineEvents(packageName, startNs, endNs)
            
            if (eventFrames.isNotEmpty()) {
                logger.info("Found ${eventFrames.size} frames from frame_timeline_event")
                return eventFrames
            }
            
            // Priority 2: Try traced sections with Choreographer
            logger.debug("Attempting to extract frames from traced sections (act: markers)")
            val tracedFrames = extractFramesFromTraceSections(packageName)
            
            if (tracedFrames.isNotEmpty()) {
                logger.info("Found ${tracedFrames.size} frames within traced sections")
                return tracedFrames
            }
            
            // Priority 3: Try actual_frame_timeline_slice (Android 12+ alternative)
            logger.debug("Trying frame_timeline_slice with time bounds")
            val sql = PerfettoQueries.frameTimingQuery(packageName, startNs, endNs)
            val results = executeQuery(sql) { row ->
                try {
                    FrameTiming(
                        vsyncId = (row["vsync_id"] as? Number)?.toLong() ?: 0,
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = (row["dur_ns"] as? Number)?.toLong() ?: 0,
                        expectedDurationNs = (row["expected_dur_ns"] as? Number)?.toLong() ?: 0,
                        isJank = (row["jank_type"] as? String) == "jank"
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid frame row: ${e.message}")
                    null
                }
            }
            
            if (results.isNotEmpty()) {
                logger.info("Found ${results.size} frames from frame_timeline_slice")
                return results
            }
            
            // Priority 4: Final fallback to Choreographer (all Android versions)
            logger.debug("No frame_timeline data, falling back to Choreographer")
            val choreographerFrames = extractChoreographerFrames(packageName, startNs, endNs)
            
            if (choreographerFrames.isNotEmpty()) {
                logger.info("Found ${choreographerFrames.size} frames from Choreographer")
            } else {
                logger.warn("No frames found from any source")
            }
            
            choreographerFrames
        } catch (e: Exception) {
            logger.warn("Failed to extract frame timing: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Extract frame timing from actual_frame_timeline_event (Android 12+).
     * This is the primary method for Android 12+ devices.
     */
    private fun extractFrameTimelineEvents(
        packageName: String,
        startNs: Long?,
        endNs: Long?
    ): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.frameTimingEventQuery(packageName, startNs, endNs)
            executeQuery(sql) { row ->
                try {
                    FrameTiming(
                        vsyncId = (row["vsync_id"] as? Number)?.toLong() ?: 0,
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = (row["dur_ns"] as? Number)?.toLong() ?: 0,
                        expectedDurationNs = (row["expected_dur_ns"] as? Number)?.toLong() ?: 0,
                        isJank = (row["jank_type"] as? String) == "jank"
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid frame_timeline_event row: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.debug("Failed to extract frame_timeline_event: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract frames that occurred during traced sections (act: markers).
     * This provides accurate FPS measurement for specific screens/flows.
     */
    private fun extractFramesFromTraceSections(packageName: String): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.framesInTraceSectionsQuery(packageName, "act:%")
            executeQuery(sql) { row ->
                try {
                    FrameTiming(
                        vsyncId = (row["vsync_id"] as? Number)?.toLong() ?: 0,
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = (row["dur_ns"] as? Number)?.toLong() ?: 0,
                        expectedDurationNs = (row["expected_dur_ns"] as? Number)?.toLong() ?: 0,
                        isJank = (row["jank_type"] as? String) == "jank"
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid traced frame row: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract frames from trace sections: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract frame timing from Choreographer (pre-Android 12).
     */
    private fun extractChoreographerFrames(
        packageName: String,
        startNs: Long?,
        endNs: Long?
    ): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.choreographerFrameQuery(packageName, startNs, endNs)
            
            // Assume 60 FPS target (16.67ms per frame)
            val targetDurationNs = 16_666_666L
            
            executeQuery(sql) { row ->
                try {
                    val durationNs = (row["dur_ns"] as? Number)?.toLong() ?: 0
                    FrameTiming(
                        vsyncId = 0, // Not available in Choreographer
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = durationNs,
                        expectedDurationNs = targetDurationNs,
                        isJank = durationNs > targetDurationNs * 1.5
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid Choreographer frame: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract Choreographer frames: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract memory usage over time.
     */
    fun extractMemoryUsage(
        packageName: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<MemoryUsage> {
        return try {
            val sql = PerfettoQueries.memoryUsageQuery(packageName, startNs, endNs)
            executeQuery(sql) { row ->
                try {
                    MemoryUsage(
                        timestampNs = (row["timestamp_ns"] as? Number)?.toLong() ?: 0,
                        rssKb = (row["rss_kb"] as? Number)?.toLong() ?: 0
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid memory row: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract memory usage: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract CPU utilization.
     */
    fun extractCpuUtilization(
        packageName: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<CpuUtilization> {
        return try {
            val sql = PerfettoQueries.cpuUtilizationQuery(packageName, startNs, endNs)
            executeQuery(sql) { row ->
                try {
                    CpuUtilization(
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = (row["dur"] as? Number)?.toLong() ?: 0,
                        threadName = row["thread_name"] as? String ?: "",
                        processName = row["process_name"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid CPU row: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract CPU utilization: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract custom trace sections (app markers).
     */
    fun extractTraceSections(
        namePattern: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<TraceSection> {
        return try {
            val sql = PerfettoQueries.traceSectionQuery(namePattern, startNs, endNs)
            executeQuery(sql) { row ->
                try {
                    TraceSection(
                        name = row["name"] as? String ?: "",
                        timestampNs = (row["ts"] as? Number)?.toLong() ?: 0,
                        durationNs = (row["dur_ns"] as? Number)?.toLong() ?: 0
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid trace section: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract trace sections: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Calculate total trace duration.
     */
    private fun calculateTotalDuration(): Long {
        return try {
            val sql = PerfettoQueries.traceBoundsQuery()
            val results = executeQuery(sql) { row ->
                (row["duration_ms"] as? Number)?.toLong() ?: 0
            }
            results.firstOrNull() ?: 0
        } catch (e: Exception) {
            logger.warn("Failed to calculate trace duration: ${e.message}")
            0
        }
    }
    
    /**
     * Determine the source of frame data for logging purposes.
     */
    private fun determineFrameSource(frames: List<FrameTiming>): String {
        return when {
            frames.firstOrNull()?.vsyncId != 0L -> "frame_timeline (vsync)"
            frames.size > 100 -> "frame_timeline_event or Choreographer"
            else -> "Choreographer"
        }
    }
    
    override fun close() {
        logger.info("Closing PerfettoMetricsExtractor")
        serverHandle?.close()
        serverHandle = null
    }
}

