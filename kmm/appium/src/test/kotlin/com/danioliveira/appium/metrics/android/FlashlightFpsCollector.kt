package com.danioliveira.appium.metrics.android

import org.slf4j.LoggerFactory

/**
 * Improved FPS calculation based on Flashlight's approach.
 * 
 * Reference: https://github.com/bamlab/flashlight/blob/main/packages/platforms/android/src/commands/gfxInfo/pollFpsUsage.ts
 * 
 * Key improvements:
 * 1. Enables FPS debug mode for detailed frame timing
 * 2. Parses actual frame timing data (Draw, Prepare, Process, Execute)
 * 3. Accounts for idle time (when app isn't rendering)
 * 4. More accurate FPS calculation
 */
class FlashlightFpsCollector(private val packageName: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val timeIntervalMs = 500L
    
    init {
        enableFpsDebug()
    }
    
    /**
     * Enable FPS debug mode for detailed frame timing data.
     * This sets the HWUI (Hardware UI) profile property.
     */
    private fun enableFpsDebug() {
        try {
            execAdbCommand("shell", "setprop", "debug.hwui.profile", "true")
            logger.info("✅ Enabled FPS debug mode (debug.hwui.profile=true)")
        } catch (e: Exception) {
            logger.warn("Failed to enable FPS debug mode: ${e.message}")
        }
    }
    
    /**
     * Collect FPS using Flashlight's algorithm.
     * 
     * Algorithm:
     * 1. Parse frame timing data from gfxinfo
     * 2. Calculate total render time
     * 3. Calculate idle time (when nothing is rendering)
     * 4. Count idle frames as 60 FPS (optimistic assumption)
     * 5. Calculate final FPS: (actualFrames + idleFrames) / totalTime
     */
    fun collectFps(): FlashlightFpsMetrics {
        return try {
            val output = execAdbCommand("shell", "dumpsys", "gfxinfo", packageName)
            processOutput(output)
        } catch (e: Exception) {
            logger.error("Failed to collect FPS: ${e.message}")
            FlashlightFpsMetrics.empty()
        }
    }
    
    /**
     * Process gfxinfo output using Flashlight's algorithm.
     */
    private fun processOutput(result: String): FlashlightFpsMetrics {
        val lines = result.split(Regex("\r\n|\n|\r"))
        
        // Find the frame timing table header
        val headerIndex = lines.indexOfFirst { line ->
            line.contains("Draw") && 
            line.contains("Prepare") && 
            line.contains("Process") && 
            line.contains("Execute")
        }
        
        if (headerIndex == -1) {
            logger.warn("FPS data not found in gfxinfo output, defaulting to 0")
            logger.warn("Make sure debug.hwui.profile is enabled")
            return FlashlightFpsMetrics(
                fps = 0.0,
                frameCount = 0,
                idleFrameCount = 0.0,
                renderTimeMs = 0.0,
                idleTimeMs = 0.0
            )
        }
        
        // Parse frame timing data
        val firstRowIndex = headerIndex + 1
        val frameTimes = mutableListOf<Double>()
        
        for (i in firstRowIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) break
            
            // Parse frame timing: "Draw Prepare Process Execute"
            // Each value is in nanoseconds, we need milliseconds
            val parts = line.split(Regex("\\s+")).mapNotNull { it.toDoubleOrNull() }
            if (parts.isNotEmpty()) {
                // Sum all timing phases (Draw + Prepare + Process + Execute)
                val totalFrameTime = parts.sum() / 1_000_000.0 // ns to ms
                frameTimes.add(totalFrameTime)
            }
        }
        
        if (frameTimes.isEmpty()) {
            logger.debug("No frame timing data found")
            return FlashlightFpsMetrics(
                fps = 60.0, // Assume 60 FPS if no frames (idle)
                frameCount = 0,
                idleFrameCount = (timeIntervalMs * 60.0) / 1000.0,
                renderTimeMs = 0.0,
                idleTimeMs = timeIntervalMs.toDouble()
            )
        }
        
        // Calculate render time
        // Each frame should take at least 16.67ms (60 FPS target)
        val targetFrameTime = 1000.0 / 60.0 // 16.67ms
        val renderTime = frameTimes.sumOf { maxOf(it, targetFrameTime) }
        val frameCount = frameTimes.size
        
        // Calculate idle time
        // If render time < interval, the rest is idle time
        val idleTime = maxOf(timeIntervalMs - renderTime, 0.0)
        val idleFrameCount = (idleTime * 60.0) / 1000.0
        
        // Calculate FPS
        // FPS = (actualFrames + idleFrames) / totalTime * 1000
        val totalTime = renderTime + idleTime
        val fps = if (totalTime > 0) {
            ((frameCount + idleFrameCount) / totalTime) * 1000.0
        } else {
            60.0
        }
        
        logger.debug("FPS calculation: frames=$frameCount, idle=${String.format("%.1f", idleFrameCount)}, " +
            "render=${String.format("%.1f", renderTime)}ms, idle=${String.format("%.1f", idleTime)}ms, fps=${String.format("%.1f", fps)}")
        
        return FlashlightFpsMetrics(
            fps = fps,
            frameCount = frameCount,
            idleFrameCount = idleFrameCount,
            renderTimeMs = renderTime,
            idleTimeMs = idleTime
        )
    }
    
    private fun execAdbCommand(vararg args: String): String {
        val adbPath = findAdbPath()
        val command = listOf(adbPath) + args
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }
    
    private fun findAdbPath(): String {
        val androidHome = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        return if (androidHome != null) {
            "$androidHome/platform-tools/adb"
        } else {
            "adb"
        }
    }
}

/**
 * FPS metrics using Flashlight's algorithm.
 */
data class FlashlightFpsMetrics(
    val fps: Double,
    val frameCount: Int,
    val idleFrameCount: Double,
    val renderTimeMs: Double,
    val idleTimeMs: Double
) {
    companion object {
        fun empty() = FlashlightFpsMetrics(0.0, 0, 0.0, 0.0, 0.0)
    }
}




