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
    
    init {
        // Initialize server once
        try {
            logger.info("Initializing TraceProcessor server")
            serverHandle = TraceProcessor.startServer(
                lifecycleManager,
                eventCallback = object : TraceProcessor.EventCallback {
                    override fun onLoadTraceFailure(trace: PerfettoTrace, throwable: Throwable) {
                        logger.error("Failed to load trace ${trace.path}: ${throwable.message}", throwable)
                    }
                },
                tracer = object : TraceProcessor.Tracer() {
                    override fun beginTraceSection(label: String) {}
                    override fun endTraceSection() {}
                }
            )
            logger.info("TraceProcessor server started successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize TraceProcessor server: ${e.message}", e)
            // serverHandle remains null, queries will return empty
        }
    }
    
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
        
        // Ensure server is started
        val currentServerHandle = serverHandle ?: run {
            logger.error("TraceProcessor server not initialized. Cannot extract metrics.")
            return TraceMetrics(traceFile = traceFile, screens = emptyList(), totalDurationMs = 0) // Return empty metrics
        }
        
        val processor = currentServerHandle.traceProcessor
        
        // Load trace ONCE and execute ALL queries within the single session
        return processor.loadTrace(
            trace = PerfettoTrace(traceFile.absolutePath),
            block = fun(session: TraceProcessor.Session): TraceMetrics {
                logger.info("Trace loaded successfully into session. Extracting metrics...")
                // Now execute all metric extractions using this single session
                return extractMetricsWithSession(session, packageName, startNs, endNs)
            }
        )
    }
    
    /**
     * Extract all metrics using a single loaded session.
     * This avoids the "Failed unrecoverably" error from loading the trace multiple times.
     */
    private fun extractMetricsWithSession(
        session: TraceProcessor.Session,
        packageName: String,
        startNs: Long?,
        endNs: Long?
    ): TraceMetrics {
        logger.info("Extracting metrics for package: $packageName")
        
        // 1. Identify the app process
        val upid = findAppProcessId(session, packageName)
        
        if (upid == null) {
            logger.error("Could not find process for package: $packageName")
            return TraceMetrics(traceFile = traceFile, screens = emptyList(), totalDurationMs = 0)
        }
        
        logger.info("Found app process UPID: $upid")

        // Extract individual metrics using UPID
        val startup = extractStartupTiming(session, upid)
        val frames = extractFrameTiming(session, upid, startNs, endNs)
        val fpsPerSecondData = extractFpsPerSecond(session, upid, startNs, endNs)
        val memory = extractMemoryUsage(session, upid, startNs, endNs)
        val cpu = extractCpuUtilization(session, upid, startNs, endNs)
        val sections = extractTraceSections(session, "act:", startNs, endNs)



        
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
            totalDurationMs = sections.sumOf { it.durationMs }.toLong(),
            cpuUtilization = cpuUtilizationList,
            startupTimeMs = startup?.durationMs?.toLong(),
            fps = avgFps,
            frameCount = totalFrames,
            screenMetrics = screenMetricsList,
            fpsPerSecond = fpsPerSecondList,
            memoryUsage = memory,
            traceStartTs = sections.minOfOrNull { it.timestampMs }
        )
    }
    
    /**
     * Calculate performance metrics for a specific screen section.
     */
    private fun calculateScreenMetrics(
        section: TraceSection,
        cpu: List<RawCpuUtilization>,
        memory: List<com.danioliveira.appium.metrics.android.MemoryUsage>,
        frames: List<FrameTiming>,
        packageName: String
    ): com.danioliveira.appium.metrics.android.ScreenMetrics {
        val sectionStartNs = section.timestampNs
        val sectionEndNs = section.timestampNs + section.durationNs
        
        // Diagnostic logging to understand timestamp filtering
        if (cpu.isNotEmpty()) {
            val cpuMinTs = cpu.minOf { it.timestampNs }
            val cpuMaxTs = cpu.maxOf { it.timestampNs }
            logger.debug("Section '${section.name}': range [$sectionStartNs, $sectionEndNs] (${(sectionEndNs - sectionStartNs) / 1_000_000}ms)")
            logger.debug("  CPU data: ${cpu.size} samples, range [$cpuMinTs, $cpuMaxTs]")
            logger.debug("  Overlap: ${if (cpuMaxTs >= sectionStartNs && cpuMinTs <= sectionEndNs) "YES" else "NO"}")
        }
        
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
     * Calculate percentile from a list of values using Apache Commons Math.
     * @param values List of numeric values
     * @param percentile Percentile to calculate (0.0 to 1.0, e.g., 0.50 for p50, 0.90 for p90)
     * @return The percentile value
     */
    private fun calculatePercentile(values: List<Double>, percentile: Double): Double {
        if (values.isEmpty()) return 0.0
        val stats = org.apache.commons.math3.stat.descriptive.DescriptiveStatistics()
        values.forEach { stats.addValue(it) }
        return stats.getPercentile(percentile * 100.0)
    }
    
    /**
     * Bucketize CPU scheduling slices into time windows and calculate CPU percentage per window.
     * This converts raw sched slices into a time series of CPU utilization percentages.
     */
    private fun bucketizeCpuToPercentages(
        cpu: List<RawCpuUtilization>,
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
        cpu: List<RawCpuUtilization>,
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
     * Execute a SQL query using the provided session (not loading the trace again).
     */
    private fun <T> sessionQuery(session: TraceProcessor.Session, sql: String, transform: (Map<String, Any?>) -> T?): List<T> {
        return try {
            logger.debug("Executing SQL query (${sql.length} chars): ${sql.take(100)}...")
            val queryResult = session.query(sql)
            
            // Convert query result to list of transformed objects
            val resultList = mutableListOf<T>()
            queryResult.forEach { row ->
                // Row already implements Map<String, Any?>, so we can use it directly
                transform(row)?.let { resultList.add(it) }
            }
            resultList
        } catch (e: Exception) {
            logger.error("Query failed: ${e.message}", e)
            emptyList()
        }
    }

    // Old executeQuery method removed - now using sessionQuery with explicit session parameter
    
    /**
 * Extract startup timing.
 */
fun extractStartupTiming(session: TraceProcessor.Session, upid: Long): StartupTiming? {
    return try {
        val sql = PerfettoQueries.startupTimingQuery(upid)
        logger.debug("Querying startup timing for UPID: $upid")
        val results = sessionQuery(session, sql) { row ->
            val type = row["startup_type"] as? String ?: "unknown"
            val durationMs = (row["duration_ms"] as? Number)?.toDouble() ?: 0.0
            val startMs = (row["start_ms"] as? Number)?.toDouble() ?: 0.0
            logger.debug("Found startup: type=$type, duration=${durationMs}ms, start=${startMs}ms")
            StartupTiming(
                type = type,
                durationMs = durationMs,
                startMs = startMs
            )
        }
        val result = results.firstOrNull()
        if (result == null) {
            logger.warn("No startup timing slices found for UPID $upid")
        } else {
            logger.info("Startup timing: ${result.type} took ${result.durationMs}ms")
        }
        result
    } catch (e: Exception) {
        logger.warn("Failed to extract startup timing: ${e.message}")
        null
    }
}
    
    /**
     * Extract jank statistics from frame timing.
     */
    fun extractJankStats(
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long? = null,
        endNs: Long? = null
    ): JankStats {
        val frames = extractFrameTiming(session, upid, startNs, endNs)
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
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<Pair<Long, Int>> {
        return try {
            // First get all frames
            val frames = extractFrameTiming(session, upid, startNs, endNs)
            
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
     * Extract memory usage over time.
     */
    fun extractMemoryUsage(
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<com.danioliveira.appium.metrics.android.MemoryUsage> {
        return try {
            val sql = PerfettoQueries.memoryUsageQuery(upid, startNs, endNs)
            sessionQuery(session, sql) { row ->
                try {
                    com.danioliveira.appium.metrics.android.MemoryUsage(
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
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<RawCpuUtilization> {
        return try {
            val sql = PerfettoQueries.cpuUtilizationQuery(upid, startNs, endNs)
            sessionQuery(session, sql) { row ->
                try {
                    RawCpuUtilization(
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
     * Extract trace sections (custom trace markers).
     */
    fun extractTraceSections(
        session: TraceProcessor.Session,
        namePrefix: String,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<TraceSection> {
        return try {
            val sql = PerfettoQueries.traceSectionQuery(namePrefix, startNs, endNs)
            val sections = sessionQuery(session, sql) { row ->
                try {
                    val name = row["name"] as? String ?: ""
                    val timestampNs = (row["ts"] as? Number)?.toLong() ?: 0
                    val durationNs = (row["dur"] as? Number)?.toLong() ?: 0
                    
                    // Diagnostic logging
                    logger.debug("Trace section: name='$name', ts=$timestampNs, dur=$durationNs (${durationNs / 1_000_000}ms)")
                    
                    TraceSection(
                        name = name,
                        timestampNs = timestampNs,
                        durationNs = durationNs
                    )
                } catch (e: Exception) {
                    logger.debug("Skipping invalid trace section row: ${e.message}")
                    null
                }
            }
            
            logger.info("Extracted ${sections.size} trace sections matching '$namePrefix'")
            if (sections.isNotEmpty()) {
                logger.debug("  First section: ${sections.first().name}, dur=${sections.first().durationMs}ms")
                logger.debug("  Last section: ${sections.last().name}, dur=${sections.last().durationMs}ms")
            }
            
            sections
        } catch (e: Exception) {
            logger.warn("Failed to extract trace sections: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract frame timing with proper fallback chain for different Android versions.
     * Priority: frame_timeline_event (Android 12+) > traced sections > frame_timeline_slice > Choreographer
     */
    fun extractFrameTiming(
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long? = null,
        endNs: Long? = null
    ): List<FrameTiming> {
        return try {
            // Priority 1: Try actual_frame_timeline_event (Android 12+)
            logger.debug("Attempting to extract frames from actual_frame_timeline_event (Android 12+)")
            val eventFrames = extractFrameTimelineEvents(session, upid, startNs, endNs)
            
            if (eventFrames.isNotEmpty()) {
                logger.info("Found ${eventFrames.size} frames from frame_timeline_event")
                return eventFrames
            }
            
            // Priority 2: Try traced sections with Choreographer
            logger.debug("Attempting to extract frames from traced sections (act: markers)")
            val tracedFrames = extractFramesFromTraceSections(session, upid)
            
            if (tracedFrames.isNotEmpty()) {
                logger.info("Found ${tracedFrames.size} frames within traced sections")
                return tracedFrames
            }
            
            // Priority 3: Try actual_frame_timeline_slice (Android 12+ alternative)
            logger.debug("Trying frame_timeline_slice with time bounds")
            val sql = PerfettoQueries.frameTimingQuery(upid, startNs, endNs)
            val results = sessionQuery(session, sql) { row ->
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
            val choreographerFrames = extractChoreographerFrames(session, upid, startNs, endNs)
            
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
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long?,
        endNs: Long?
    ): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.frameTimingEventQuery(upid, startNs, endNs)
            sessionQuery(session, sql) { row ->
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
    private fun extractFramesFromTraceSections(session: TraceProcessor.Session, upid: Long): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.framesInTraceSectionsQuery(upid, "act:%")
            sessionQuery(session, sql) { row ->
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
        session: TraceProcessor.Session,
        upid: Long,
        startNs: Long?,
        endNs: Long?
    ): List<FrameTiming> {
        return try {
            val sql = PerfettoQueries.choreographerFrameQuery(upid, startNs, endNs)
            
            // Assume 60 FPS target (16.67ms per frame)
            val targetDurationNs = 16_666_666L
            
            sessionQuery(session, sql) { row ->
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
    
    // calculateTraceBounds method removed - not used in session-based flow
    
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
    
    /**
     * Find the app process ID (upid) using multiple strategies.
     * 1. Exact match or LIKE match on process.name
     * 2. Thread name matching package name suffix (for truncated names)
     * 3. Command line arguments
     */
    private fun findAppProcessId(session: TraceProcessor.Session, packageName: String): Long? {
        // Strategy 1: Try process.name
        val nameSql = "SELECT upid FROM process WHERE name LIKE '%$packageName%' LIMIT 1"
        val nameResult = sessionQuery(session, nameSql) { row ->
            (row["upid"] as? Number)?.toLong()
        }
        if (nameResult.isNotEmpty()) return nameResult.first()
        
        // Strategy 2: Try thread name matching package suffix
        // Process names are often truncated to 15 chars in some contexts, or threads inherit the name
        val suffix = if (packageName.length > 15) packageName.takeLast(15) else packageName
        val threadSql = """
            SELECT process.upid 
            FROM thread 
            JOIN process USING(upid) 
            WHERE thread.name LIKE '%$suffix%' 
            LIMIT 1
        """
        val threadResult = sessionQuery(session, threadSql) { row ->
            (row["upid"] as? Number)?.toLong()
        }
        if (threadResult.isNotEmpty()) return threadResult.first()
        
        // Strategy 3: Try cmdline (via args)
        val cmdlineSql = """
            SELECT process.upid
            FROM process
            JOIN args ON process.arg_set_id = args.arg_set_id
            WHERE args.key = 'cmdline' AND args.string_value LIKE '%$packageName%'
            LIMIT 1
        """
        val cmdlineResult = sessionQuery(session, cmdlineSql) { row ->
            (row["upid"] as? Number)?.toLong()
        }
        if (cmdlineResult.isNotEmpty()) return cmdlineResult.first()
        
        return null
    }

    override fun close() {
        logger.info("Closing PerfettoMetricsExtractor")
        serverHandle?.close()
        serverHandle = null
    }


    // Legacy compatibility methods (for backward compatibility with old code)
    // These are deprecated and should not be used - they load the trace for every call
    @Deprecated("Use extractMetrics() instead which loads trace once")
    fun extractMemoryUsage(packageName: String, startNs: Long? = null, endNs: Long? = null): List<com.danioliveira.appium.metrics.android.MemoryUsage> = emptyList()
    
    @Deprecated("Use extractMetrics() instead which loads trace once")
    fun extractCpuUtilization(packageName: String, startNs: Long? = null, endNs: Long? = null): List<RawCpuUtilization> = emptyList()
    
    @Deprecated("Use extractMetrics() instead which loads trace once")
    fun extractTraceSections(namePattern: String, startNs: Long? = null, endNs: Long? = null): List<TraceSection> = emptyList()
    
    @Deprecated("Use extractMetrics() instead which loads trace once")
    fun extractFrameTiming(packageName: String, startNs: Long? = null, endNs: Long? = null): List<FrameTiming> = emptyList()
    
    @Deprecated("Use extractMetrics() instead which loads trace once")
    fun extractFpsPerSecond(packageName: String, startNs: Long? = null, endNs: Long? = null): List<Pair<Long, Int>> = emptyList()
}

