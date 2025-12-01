package com.danioliveira.appium.metrics.android.perfetto

/**
 * Jank statistics extracted from Perfetto traces.
 * 
 * Jank is defined as frames that take significantly longer than expected.
 * Based on AndroidX Macrobenchmark jank detection.
 */
data class JankStats(
    val totalFrames: Int,
    val jankFrames: Int,
    val jankPercentage: Double,
    val avgFrameTimeMs: Double,
    val maxFrameTimeMs: Double,
    val p50FrameTimeMs: Double,
    val p90FrameTimeMs: Double,
    val p95FrameTimeMs: Double,
    val p99FrameTimeMs: Double
) {
    companion object {
        fun empty() = JankStats(
            totalFrames = 0,
            jankFrames = 0,
            jankPercentage = 0.0,
            avgFrameTimeMs = 0.0,
            maxFrameTimeMs = 0.0,
            p50FrameTimeMs = 0.0,
            p90FrameTimeMs = 0.0,
            p95FrameTimeMs = 0.0,
            p99FrameTimeMs = 0.0
        )
        
        /**
         * Calculate jank stats from frame timings.
         * 
         * @param frames List of frame timings
         * @return JankStats
         */
        fun from(frames: List<FrameTiming>): JankStats {
            if (frames.isEmpty()) return empty()
            
            val frameTimesMs = frames.map { it.durationMs }.sorted()
            val jankCount = frames.count { it.isJank }
            
            return JankStats(
                totalFrames = frames.size,
                jankFrames = jankCount,
                jankPercentage = (jankCount.toDouble() / frames.size) * 100.0,
                avgFrameTimeMs = frameTimesMs.average(),
                maxFrameTimeMs = frameTimesMs.last(),
                p50FrameTimeMs = percentile(frameTimesMs, 50.0),
                p90FrameTimeMs = percentile(frameTimesMs, 90.0),
                p95FrameTimeMs = percentile(frameTimesMs, 95.0),
                p99FrameTimeMs = percentile(frameTimesMs, 99.0)
            )
        }
        
        private fun percentile(sorted: List<Double>, percentile: Double): Double {
            if (sorted.isEmpty()) return 0.0
            val index = ((percentile / 100.0) * (sorted.size - 1)).toInt()
            return sorted[index.coerceIn(0, sorted.size - 1)]
        }
    }
}

/**
 * Startup timing breakdown from Perfetto.
 * 
 * Based on AndroidX Macrobenchmark StartupTimingQuery.
 */
data class StartupBreakdown(
    val type: String,           // "cold", "warm", or "hot"
    val totalMs: Double,
    val phases: List<StartupPhase>
) {
    companion object {
        fun empty() = StartupBreakdown(
            type = "unknown",
            totalMs = 0.0,
            phases = emptyList()
        )
    }
}

/**
 * A phase within app startup.
 */
data class StartupPhase(
    val name: String,
    val durationMs: Double,
    val percentageOfTotal: Double
)


