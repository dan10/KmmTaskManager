package com.danioliveira.appium.metrics.android.perfetto

import org.slf4j.LoggerFactory

/**
 * SQL queries for extracting metrics from Perfetto traces.
 * 
 * Based on AndroidX Macrobenchmark queries:
 * - MemoryUsageQuery.kt
 * - StartupTimingQuery.kt
 * - FrameTimingQuery.kt
 * - JankCollectionHelper.java
 * 
 * Reference: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:benchmark/benchmark-macro/
 */
object PerfettoQueries {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Query memory usage (RSS) for a process over time.
     * 
     * Returns: timestamp_ns, rss_kb
     */
    fun memoryUsageQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)
        
        return """
            SELECT
                ts as timestamp_ns,
                process.name as process_name,
                counter.value / 1024 as rss_kb
            FROM counter
            JOIN process_counter_track ON counter.track_id = process_counter_track.id
            JOIN process USING(upid)
            WHERE process_counter_track.name = 'mem.rss'
                AND process.name LIKE '%$packageName%'
                $timeFilter
            ORDER BY ts
        """.trimIndent()
    }
    
    /**
     * Query startup timing for an app.
     * 
     * Returns: startup_type (cold/warm/hot), duration_ms
     * 
     * Based on AndroidX StartupTimingQuery.kt
     */
    fun startupTimingQuery(packageName: String): String {
        return """
            SELECT
                slice.name as startup_type,
                slice.dur / 1000000.0 as duration_ms,
                slice.ts / 1000000.0 as start_ms
            FROM slice
            WHERE slice.name GLOB 'launching: $packageName'
                OR slice.name GLOB 'activityStart'
                OR slice.name GLOB 'reportFullyDrawn'
            ORDER BY slice.ts
            LIMIT 1
        """.trimIndent()
    }
    
    /**
     * Query frame timing using frame_timeline (Android 12+).
     *
     * Returns: vsync_id, dur_ns, jank_type
     *
     * Based on AndroidX FrameTimingQuery.kt
     */
    fun frameTimingQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)

        return """
            SELECT
                actual_frame_timeline_slice.name,
                actual_frame_timeline_slice.ts,
                actual_frame_timeline_slice.dur as dur_ns,
                COALESCE(actual_frame_timeline_slice.surface_frame_token, actual_frame_timeline_slice.display_frame_token, 0) as vsync_id,
                expected_frame_timeline_slice.dur as expected_dur_ns,
                CASE
                    WHEN actual_frame_timeline_slice.dur > expected_frame_timeline_slice.dur * 1.5
                        THEN 'jank'
                    ELSE 'normal'
                END as jank_type
            FROM actual_frame_timeline_slice
            JOIN expected_frame_timeline_slice ON (
                actual_frame_timeline_slice.surface_frame_token = expected_frame_timeline_slice.surface_frame_token
                AND actual_frame_timeline_slice.display_frame_token = expected_frame_timeline_slice.display_frame_token
            )
            JOIN process_track ON actual_frame_timeline_slice.track_id = process_track.id
            JOIN process USING(upid)
            WHERE process.name LIKE '%$packageName%'
                $timeFilter
            ORDER BY actual_frame_timeline_slice.ts
        """.trimIndent()
    }
    
    /**
     * Query frame timing using actual_frame_timeline_slice (Android 12+).
     *
     * NOTE:
     * - Some devices/traces don't expose the `actual_frame_timeline_event` table, but
     *   they do expose `actual_frame_timeline_slice` and `expected_frame_timeline_slice`.
     * - We treat this as the primary method for Android 12+ devices when available.
     * - The tables are joined on surface_frame_token and display_frame_token
     *
     * Returns: ts, dur_ns, vsync_id, expected_dur_ns, jank_type
     */
    fun frameTimingEventQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)

        return """
            SELECT
                actual_frame_timeline_slice.ts,
                actual_frame_timeline_slice.dur as dur_ns,
                COALESCE(actual_frame_timeline_slice.surface_frame_token, actual_frame_timeline_slice.display_frame_token, 0) as vsync_id,
                expected_frame_timeline_slice.dur as expected_dur_ns,
                CASE
                    WHEN actual_frame_timeline_slice.dur > expected_frame_timeline_slice.dur * 1.5
                        THEN 'jank'
                    ELSE 'normal'
                END as jank_type
            FROM actual_frame_timeline_slice
            JOIN expected_frame_timeline_slice ON (
                actual_frame_timeline_slice.surface_frame_token = expected_frame_timeline_slice.surface_frame_token
                AND actual_frame_timeline_slice.display_frame_token = expected_frame_timeline_slice.display_frame_token
            )
            JOIN process_track ON actual_frame_timeline_slice.track_id = process_track.id
            JOIN process USING(upid)
            WHERE process.name LIKE '%$packageName%'
                $timeFilter
            ORDER BY actual_frame_timeline_slice.ts
        """.trimIndent()
    }
    
    /**
     * Query FPS aggregated per second using actual_frame_timeline_slice (Android 12+).
     * This provides a time-series view of FPS over the trace duration.
     *
     * Returns: second (timestamp in seconds), fps (frame count per second)
     */
    fun fpsPerSecondQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = when {
            startNs != null && endNs != null -> "AND actual_frame_timeline_slice.ts BETWEEN $startNs AND $endNs"
            startNs != null -> "AND actual_frame_timeline_slice.ts >= $startNs"
            endNs != null -> "AND actual_frame_timeline_slice.ts <= $endNs"
            else -> ""
        }

        return """
            SELECT
                (actual_frame_timeline_slice.ts + actual_frame_timeline_slice.dur) / 1000000000 AS second,
                COUNT(*) AS fps
            FROM actual_frame_timeline_slice
            JOIN process_track ON actual_frame_timeline_slice.track_id = process_track.id
            JOIN process USING(upid)
            WHERE process.name LIKE '%$packageName%'
                $timeFilter
            GROUP BY second
            ORDER BY second
        """.trimIndent()
    }
    
    /**
     * Query frames that occurred during specific trace sections (e.g., "act:" markers).
     * This correlates frame timing with custom trace markers to get FPS during specific screens.
     * 
     * Uses Choreographer frames as they're more reliable across Android versions.
     * 
     * Returns: frame timing data within the bounds of trace sections
     */
    fun framesInTraceSectionsQuery(
        packageName: String,
        sectionNamePattern: String = "act:%"
    ): String {
        return """
            WITH trace_sections AS (
                SELECT
                    slice.name as section_name,
                    slice.ts as section_start,
                    slice.ts + slice.dur as section_end
                FROM slice
                WHERE slice.name LIKE '$sectionNamePattern'
            ),
            choreographer_frames AS (
                SELECT
                    slice.ts,
                    slice.dur as dur_ns
                FROM slice
                JOIN thread_track ON slice.track_id = thread_track.id
                JOIN thread USING(utid)
                JOIN process USING(upid)
                WHERE slice.name LIKE 'Choreographer#doFrame%'
                    AND process.name LIKE '%$packageName%'
            )
            SELECT
                choreographer_frames.ts,
                choreographer_frames.dur_ns,
                0 as vsync_id,
                16666666 as expected_dur_ns,
                trace_sections.section_name,
                CASE
                    WHEN choreographer_frames.dur_ns > 16666666 * 1.5
                        THEN 'jank'
                    ELSE 'normal'
                END as jank_type
            FROM choreographer_frames
            JOIN trace_sections ON (
                choreographer_frames.ts >= trace_sections.section_start
                AND choreographer_frames.ts <= trace_sections.section_end
            )
            ORDER BY choreographer_frames.ts
        """.trimIndent()
    }
    
    /**
     * Query frame timing using Choreographer (pre-Android 12 fallback).
     * 
     * Returns: ts, dur_ns
     */
    fun choreographerFrameQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)
        
        return """
            SELECT
                slice.ts,
                slice.dur as dur_ns
            FROM slice
            JOIN thread_track ON slice.track_id = thread_track.id
            JOIN thread USING(utid)
            JOIN process USING(upid)
            WHERE slice.name LIKE 'Choreographer#doFrame%'
                AND process.name LIKE '%$packageName%'
                $timeFilter
            ORDER BY slice.ts
        """.trimIndent()
    }
    
    /**
     * Query CPU utilization for a process.
     * 
     * Returns: ts, cpu_percent, thread_name
     */
    fun cpuUtilizationQuery(packageName: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)
        
        return """
            SELECT
                sched.ts,
                sched.dur,
                thread.name as thread_name,
                process.name as process_name
            FROM sched
            JOIN thread USING(utid)
            JOIN process USING(upid)
            WHERE process.name LIKE '%$packageName%'
                $timeFilter
            ORDER BY sched.ts
        """.trimIndent()
    }
    
    /**
     * Query custom trace sections (app markers like "act:LoginScreen").
     * 
     * Returns: name, ts, dur_ns
     */
    fun traceSectionQuery(sectionNamePattern: String, startNs: Long? = null, endNs: Long? = null): String {
        val timeFilter = buildTimeFilter(startNs, endNs)
        
        return """
            SELECT
                slice.name,
                slice.ts,
                slice.dur as dur_ns,
                slice.ts / 1000000.0 as start_ms,
                slice.dur / 1000000.0 as duration_ms
            FROM slice
            WHERE slice.name LIKE '%$sectionNamePattern%'
                $timeFilter
            ORDER BY slice.ts
        """.trimIndent()
    }
    
    /**
     * Query process stats summary.
     * 
     * Returns: process info and resource usage
     */
    fun processStatsQuery(packageName: String): String {
        return """
            SELECT
                process.name,
                process.pid,
                COUNT(DISTINCT thread.utid) as thread_count
            FROM process
            LEFT JOIN thread USING(upid)
            WHERE process.name LIKE '%$packageName%'
            GROUP BY process.upid
        """.trimIndent()
    }
    
    /**
     * Get trace bounds (start and end timestamps).
     */
    fun traceBoundsQuery(): String {
        return """
            SELECT
                MIN(ts) as start_ns,
                MAX(ts) as end_ns,
                (MAX(ts) - MIN(ts)) / 1000000.0 as duration_ms
            FROM slice
        """.trimIndent()
    }
    
    /**
     * Build time filter clause for SQL queries.
     */
    private fun buildTimeFilter(startNs: Long?, endNs: Long?): String {
        return when {
            startNs != null && endNs != null -> "AND ts BETWEEN $startNs AND $endNs"
            startNs != null -> "AND ts >= $startNs"
            endNs != null -> "AND ts <= $endNs"
            else -> ""
        }
    }
}

/**
 * Startup timing result.
 */
data class StartupTiming(
    val type: String,           // "cold", "warm", or "hot"
    val durationMs: Double,
    val startMs: Double
)

/**
 * Frame timing result.
 */
data class FrameTiming(
    val vsyncId: Long,
    val timestampNs: Long,
    val durationNs: Long,
    val expectedDurationNs: Long,
    val isJank: Boolean
) {
    val durationMs: Double get() = durationNs / 1_000_000.0
    val expectedDurationMs: Double get() = expectedDurationNs / 1_000_000.0
}

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

/**
 * CPU utilization data point.
 */
data class CpuUtilization(
    val timestampNs: Long,
    val durationNs: Long,
    val threadName: String,
    val processName: String
) {
    val timestampMs: Long get() = timestampNs / 1_000_000
    val durationMs: Double get() = durationNs / 1_000_000.0
}

/**
 * Trace section (custom marker) result.
 */
data class TraceSection(
    val name: String,
    val timestampNs: Long,
    val durationNs: Long
) {
    val timestampMs: Long get() = timestampNs / 1_000_000
    val durationMs: Double get() = durationNs / 1_000_000.0
}

/**
 * Trace bounds.
 */
data class TraceBounds(
    val startNs: Long,
    val endNs: Long,
    val durationMs: Double
)

