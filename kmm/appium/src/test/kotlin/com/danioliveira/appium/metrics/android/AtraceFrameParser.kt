package com.danioliveira.appium.metrics.android

import org.slf4j.LoggerFactory

/**
 * Parses Choreographer#doFrame events from atrace/systrace for accurate FPS calculation.
 * 
 * Based on Flashlight's atrace FPS algorithm:
 * https://github.com/bamlab/flashlight/blob/main/packages/platforms/android/src/commands/atrace/pollFpsUsage.ts
 * 
 * Key improvements over gfxinfo:
 * 1. Uses actual systrace events (Choreographer#doFrame)
 * 2. Accounts for UI thread CPU usage in idle calculation
 * 3. Better integration with existing systrace pipeline
 * 4. Single source of truth for all metrics
 */
class AtraceFrameParser {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private var methodStartedCount = 0
    private var doFrameStartedTimestamp: Double? = null
    
    /**
     * Parse frame times from atrace output.
     * Looks for Choreographer#doFrame events to track frame rendering.
     * 
     * @param output Raw atrace/systrace output
     * @param pid Process ID to filter events
     * @return Frame times and total interval
     */
    fun getFrameTimes(
        output: String,
        pid: String
    ): FrameTimesResult {
        val lines = output.split(Regex("\r\n|\n|\r")).filter { it.isNotBlank() }
        
        if (lines.isEmpty()) {
            return FrameTimesResult(emptyList(), 0.0)
        }
        
        val frameTimes = mutableListOf<Double>()
        
        lines.forEach { line ->
            try {
                // Only process lines for our PID
                if (!line.contains("-$pid ")) return@forEach
                
                val parsed = parseLine(line)
                
                if (parsed.ending) {
                    methodStartedCount--
                    if (methodStartedCount <= 0) {
                        doFrameStartedTimestamp?.let { startTime ->
                            frameTimes.add(parsed.timestamp - startTime)
                            doFrameStartedTimestamp = null
                        }
                        methodStartedCount = 0
                    }
                } else {
                    parsed.methodName?.let { method ->
                        if (method.contains("Choreographer#doFrame")) {
                            methodStartedCount = 1
                            doFrameStartedTimestamp = parsed.timestamp
                        } else {
                            methodStartedCount++
                        }
                    }
                }
            } catch (e: Exception) {
                logger.debug("Failed to parse atrace line: ${e.message}")
            }
        }
        
        val interval = if (lines.size >= 2) {
            try {
                parseLine(lines.last()).timestamp - parseLine(lines.first()).timestamp
            } catch (e: Exception) {
                0.0
            }
        } else {
            0.0
        }
        
        return FrameTimesResult(frameTimes, interval)
    }
    
    /**
     * Parse a single atrace line.
     * 
     * Supports multiple formats:
     * 1. "process-pid [cpu] timestamp: tracing_mark_write: B|E methodName"
     * 2. "process-pid [cpu] timestamp: event"
     */
    private fun parseLine(line: String): ParsedLine {
        // Try format: "timestamp: tracing_mark_write: B|E methodName"
        var regex = Regex("""\s+(\d+\.\d+):\s+tracing_mark_write:\s+([BE])(.*)""")
        var match = regex.find(line)
        
        if (match != null) {
            val (timestamp, beginOrEnd, methodName) = match.destructured
            return ParsedLine(
                timestamp = timestamp.toDouble() * 1000, // Convert to ms
                ending = beginOrEnd == "E",
                methodName = methodName.trim()
            )
        }
        
        // Try simpler format: "timestamp: event"
        regex = Regex("""\s+(\d+\.\d+):\s+(.*)""")
        match = regex.find(line)
        
        if (match != null) {
            val (timestamp, event) = match.destructured
            return ParsedLine(
                timestamp = timestamp.toDouble() * 1000,
                ending = false,
                methodName = event.trim()
            )
        }
        
        throw IllegalArgumentException("Could not parse atrace line: $line")
    }
    
    companion object {
        /**
         * Calculate FPS from frame times, accounting for UI thread CPU usage.
         * 
         * This is Flashlight's key innovation: idle time is adjusted based on UI CPU usage.
         * 
         * **Why this matters:**
         * - If UI thread is busy (high CPU) but not drawing frames, it's not truly idle
         * - The thread might be blocked or doing other work
         * - We shouldn't count this as "60 FPS idle time"
         * 
         * **Formula:**
         * ```
         * idleTime = (timeInterval - totalFrameTime) * (1 - uiCpuUsage / 100)
         * ```
         * 
         * **Examples:**
         * - uiCpuUsage = 0%: Full idle time counted (app truly idle at 60 FPS)
         * - uiCpuUsage = 50%: Half idle time counted (thread partially busy)
         * - uiCpuUsage = 100%: No idle time counted (thread fully blocked)
         * 
         * @param frameTimes List of frame durations in milliseconds
         * @param timeInterval Total time interval in milliseconds
         * @param uiCpuUsage UI thread CPU usage percentage (0-100)
         * @param targetFrameRate Target frame rate (default 60 FPS)
         * @return Calculated FPS, clamped to [0, targetFrameRate]
         */
        fun getFps(
            frameTimes: List<Double>,
            timeInterval: Double,
            uiCpuUsage: Double,
            targetFrameRate: Int = 60
        ): Double {
            if (timeInterval <= 0) return 0.0
            
            val frameCount = frameTimes.size
            val targetFrameTime = 1000.0 / targetFrameRate
            
            // Calculate total time spent rendering frames
            // Each frame takes at least targetFrameTime (16.67ms for 60 FPS)
            val totalFrameTime = frameTimes.sumOf { maxOf(targetFrameTime, it) }
            
            /**
             * Key innovation: Adjust idle time based on UI CPU usage
             * 
             * If UI thread is busy (high CPU) but not drawing frames,
             * it's not truly idle - the thread is blocked or doing other work.
             * 
             * idleTime = (timeInterval - totalFrameTime) * (1 - uiCpuUsage / 100)
             */
            val rawIdleTime = timeInterval - totalFrameTime
            val adjustedIdleTime = rawIdleTime * (1 - uiCpuUsage / 100)
            
            // Count idle frames at target frame rate
            val idleTimeFrameCount = (adjustedIdleTime / 1000) * targetFrameRate
            
            // Calculate FPS
            val fps = ((frameCount + idleTimeFrameCount) / timeInterval) * 1000
            
            // Clamp to [0, targetFrameRate]
            return maxOf(0.0, minOf(targetFrameRate.toDouble(), fps))
        }
    }
}

/**
 * Result of parsing frame times from atrace.
 */
data class FrameTimesResult(
    val frameTimes: List<Double>,
    val interval: Double
)

/**
 * Parsed atrace line.
 */
data class ParsedLine(
    val timestamp: Double,
    val ending: Boolean,
    val methodName: String?
)




