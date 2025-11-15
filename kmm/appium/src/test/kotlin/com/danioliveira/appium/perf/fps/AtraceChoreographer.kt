package com.danioliveira.appium.perf.fps

import com.danioliveira.appium.metrics.android.AtraceFrameParser
import org.slf4j.LoggerFactory

/**
 * Wrapper for Flashlight atrace/Choreographer-based FPS calculation.
 * 
 * This uses the atrace approach from Flashlight:
 * https://github.com/bamlab/flashlight/blob/main/packages/platforms/android/src/commands/atrace/pollFpsUsage.ts
 * 
 * **Pros:**
 * - Most accurate (uses actual Choreographer#doFrame events)
 * - Accounts for UI thread CPU usage in idle calculation
 * - Single source of truth (systrace has all metrics)
 * 
 * **Cons:**
 * - Requires trace parsing (post-test analysis)
 * - Needs 'view' systrace category enabled
 * - Depends on Choreographer events being present
 */
class AtraceChoreographer {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val parser = AtraceFrameParser()
    
    /**
     * Calculate FPS from systrace content.
     * 
     * @param systraceContent Raw systrace HTML content
     * @param pid Process ID to filter events
     * @param uiCpuUsage UI thread CPU usage percentage (0-100)
     * @return FPS and frame count
     */
    fun calculateFps(
        systraceContent: String,
        pid: String,
        uiCpuUsage: Double
    ): AtraceFpsResult {
        try {
            val result = parser.getFrameTimes(systraceContent, pid)
            
            if (result.frameTimes.isEmpty()) {
                logger.warn("No Choreographer#doFrame events found in trace")
                logger.warn("Make sure 'view' category is enabled in systrace")
                return AtraceFpsResult(0.0, 0)
            }
            
            val fps = AtraceFrameParser.getFps(
                frameTimes = result.frameTimes,
                timeInterval = result.interval,
                uiCpuUsage = uiCpuUsage,
                targetFrameRate = 60
            )
            
            logger.debug("Atrace FPS: ${String.format("%.1f", fps)} " +
                "(${result.frameTimes.size} frames, ${String.format("%.0f", result.interval)}ms interval, " +
                "${String.format("%.1f", uiCpuUsage)}% UI CPU)")
            
            return AtraceFpsResult(fps, result.frameTimes.size)
            
        } catch (e: Exception) {
            logger.error("Failed to calculate FPS from atrace: ${e.message}")
            return AtraceFpsResult(0.0, 0)
        }
    }
}

/**
 * FPS calculation result from atrace.
 */
data class AtraceFpsResult(
    val fps: Double,
    val frameCount: Int
)

