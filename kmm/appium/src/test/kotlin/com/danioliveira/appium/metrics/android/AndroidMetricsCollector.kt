package com.danioliveira.appium.metrics.android

import com.danioliveira.appium.utils.AdbShell
import org.slf4j.LoggerFactory
import java.io.File

class AndroidMetricsCollector(private val packageName: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var perfettoProcess: Process? = null
    
    // Systrace collector for screen-level tracing
    val systraceCollector: SystraceCollector by lazy {
        SystraceCollector()
    }

    // Perfetto collector for detailed tracing
    val perfettoCollector: PerfettoCollector by lazy {
        PerfettoCollector()
    }
    
    fun resetGfxInfo() {
        try {
            AdbShell.exec("shell", "dumpsys", "gfxinfo", packageName, "reset")
            logger.debug("Reset gfxinfo for $packageName")
        } catch (e: Exception) {
            logger.warn("Failed to reset gfxinfo: ${e.message}")
        }
    }
    
    fun collectGfxInfo(): GfxInfoMetrics {
        return try {
            val output = AdbShell.exec("shell", "dumpsys", "gfxinfo", packageName)
            parseGfxInfo(output)
        } catch (e: Exception) {
            logger.error("Failed to collect gfxinfo: ${e.message}")
            GfxInfoMetrics.empty()
        }
    }
    

    
    /**
     * Inject a custom trace marker into the system trace buffer.
     * Useful for marking start/end of test actions in Perfetto/Systrace.
     * 
     * @param label The label for the trace section (e.g. "B|pid|act:ActionName" or "E")
     */
    fun injectTraceMarker(label: String) {
        try {
            // Write to trace_marker to inject a userspace trace event
            // We use the shell's echo to write to the file
            AdbShell.execNoRetry("shell", "echo '$label' > /sys/kernel/tracing/trace_marker")
        } catch (e: Exception) {
            // Fallback to legacy path if new path fails
            try {
                AdbShell.execNoRetry("shell", "echo '$label' > /sys/kernel/debug/tracing/trace_marker")
            } catch (e2: Exception) {
                logger.debug("Failed to inject trace marker: ${e2.message}")
            }
        }
    }
    
    fun collectMemInfo(): MemInfoMetrics {
        return try {
            val output = AdbShell.exec("shell", "dumpsys", "meminfo", packageName)
            parseMemInfo(output)
        } catch (e: Exception) {
            logger.error("Failed to collect meminfo: ${e.message}")
            MemInfoMetrics.empty()
        }
    }
    
    fun collectCpuInfo(): CpuMetrics {
        return try {
            val pid = getPid()
            if (pid == null) {
                logger.warn("Could not get PID for $packageName - app may not be running")
                return CpuMetrics.empty()
            }
            
            logger.debug("Collecting CPU for PID: $pid")
            val output = AdbShell.exec("shell", "top", "-n", "1", "-p", pid.toString())
            
            if (output.isEmpty() || output.length < 50) {
                logger.warn("top command returned empty or very short output for PID $pid")
                logger.debug("top output: $output")
                return CpuMetrics.empty()
            }
            
            parseCpuInfo(output)
        } catch (e: Exception) {
            logger.error("Failed to collect CPU info: ${e.message}")
            CpuMetrics.empty()
        }
    }
    
    fun measureLaunchTime(): Long {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Force stop the app
            AdbShell.exec("shell", "am", "force-stop", packageName)
            Thread.sleep(1000)
            
            // Start the app and measure
            AdbShell.exec("shell", "am", "start", "-W", "-n", 
                "$packageName/.MainActivity")
            
            val endTime = System.currentTimeMillis()
            val launchTime = endTime - startTime
            logger.info("App launch time: ${launchTime}ms")
            launchTime
        } catch (e: Exception) {
            logger.error("Failed to measure Android launch time: ${e.message}")
            0L
        }
    }
    
    fun getPid(): Int? {
        return try {
            val output = AdbShell.exec("shell", "pidof", packageName)
            val pid = output.trim().toIntOrNull()
            if (pid != null) {
                logger.debug("Found PID $pid for $packageName")
            } else {
                logger.debug("pidof returned: '$output' for $packageName")
            }
            pid
        } catch (e: Exception) {
            logger.debug("Exception getting PID: ${e.message}")
            null
        }
    }
    
    private fun parseGfxInfo(output: String): GfxInfoMetrics {
        var jankyFrames = 0
        var totalFrames = 0
        val frameTimes = mutableListOf<Double>()
        
        val lines = output.split("\n")
        var inFrameStats = false
        
        for (line in lines) {
            when {
                line.contains("Total frames rendered:") -> {
                    totalFrames = line.substringAfter(":").trim().toIntOrNull() ?: 0
                }
                line.contains("Janky frames:") -> {
                    jankyFrames = line.substringAfter(":").trim()
                        .substringBefore("(").trim().toIntOrNull() ?: 0
                }
                line.contains("PROFILE DATA") -> {
                    inFrameStats = true
                }
                inFrameStats && line.trim().matches(Regex("\\d+\\s+\\d+.*")) -> {
                    val parts = line.trim().split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        parts[1].toDoubleOrNull()?.let { frameTimes.add(it / 1_000_000.0) }
                    }
                }
            }
        }
        
        return if (frameTimes.isNotEmpty()) {
            val sorted = frameTimes.sorted()
            GfxInfoMetrics(
                jankyFrames = jankyFrames,
                totalFrames = totalFrames,
                avgFrameTimeMs = frameTimes.average(),
                p50FrameTimeMs = sorted[sorted.size / 2],
                p90FrameTimeMs = sorted[(sorted.size * 0.9).toInt()],
                p99FrameTimeMs = sorted[(sorted.size * 0.99).toInt()]
            )
        } else {
            GfxInfoMetrics(jankyFrames, totalFrames, 0.0, 0.0, 0.0, 0.0)
        }
    }
    
    private fun parseMemInfo(output: String): MemInfoMetrics {
        var totalPssKb = 0
        var totalRssKb = 0
        var javaHeapKb = 0
        var nativeHeapKb = 0
        var graphicsKb = 0
        
        val lines = output.split("\n")
        for (line in lines) {
            when {
                // Parse "TOTAL PSS:    42027            TOTAL RSS:   131304"
                line.contains("TOTAL PSS:") -> {
                    val parts = line.split(Regex("\\s+"))
                    // Format: "TOTAL PSS:    42027            TOTAL RSS:   131304"
                    val pssIndex = parts.indexOf("PSS:") + 1
                    if (pssIndex > 0 && pssIndex < parts.size) {
                        totalPssKb = parts[pssIndex].toIntOrNull() ?: 0
                    }
                    val rssIndex = parts.indexOf("RSS:") + 1
                    if (rssIndex > 0 && rssIndex < parts.size) {
                        totalRssKb = parts[rssIndex].toIntOrNull() ?: 0
                    }
                }
                // Parse App Summary section
                line.trim().startsWith("Java Heap:") -> {
                    // Format: "Java Heap:     4684                          20932"
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 3) {
                        javaHeapKb = parts[2].toIntOrNull() ?: 0
                    }
                }
                line.trim().startsWith("Native Heap:") -> {
                    // Format: "Native Heap:     9732                          10616"
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 3) {
                        nativeHeapKb = parts[2].toIntOrNull() ?: 0
                    }
                }
                line.trim().startsWith("Graphics:") -> {
                    // Format: "Graphics:        0                              0"
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        graphicsKb = parts[1].toIntOrNull() ?: 0
                    }
                }
            }
        }
        
        // Use PSS (Proportional Set Size) as the main memory metric
        // PSS accounts for shared memory proportionally
        val totalMemMb = totalPssKb / 1024
        
        logger.debug("Parsed meminfo: PSS=${totalPssKb}KB (${totalMemMb}MB), RSS=${totalRssKb}KB, " +
            "Java=${javaHeapKb}KB, Native=${nativeHeapKb}KB, Graphics=${graphicsKb}KB")
        
        return MemInfoMetrics(
            rssMb = totalMemMb,
            javaHeapMb = javaHeapKb / 1024,
            graphicsMb = graphicsKb / 1024
        )
    }
    
    private fun parseCpuInfo(output: String): CpuMetrics {
        val lines = output.split("\n")
        
        // Package name might be truncated with '+' in top output (e.g., "com.danioliveir+")
        val packagePrefix = packageName.take(15)  // top often truncates package names
        
        for (line in lines) {
            // Match full package name or truncated version with '+'
            if (line.contains(packageName) || line.contains("$packagePrefix+")) {
                val parts = line.trim().split(Regex("\\s+"))
                logger.debug("CPU line parts (${parts.size}): ${parts.joinToString(" | ")}")
                
                // Standard Android top format (your output):
                // PID USER  PR  NI VIRT  RES  SHR S[%CPU] %MEM     TIME+ ARGS
                //  0   1     2   3   4    5    6   7  8     9       10    11
                // Example: 27797 u0_a222 10 -10 16G 126M 90M S 0.0 6.3 0:00.22 com.danioliveir+
                
                if (parts.size >= 9) {
                    // Position 8 is the CPU percentage after status (S/R/D)
                    val cpuStr = parts[8]
                    val cpuPercent = cpuStr.replace("%", "").toDoubleOrNull()
                    
                    if (cpuPercent != null && cpuPercent >= 0 && cpuPercent <= 400) {  // Allow >100% for multi-core
                        logger.info("Found CPU: $cpuPercent% for $packageName (line matched)")
                        return CpuMetrics(cpuPercent)
                    }
                }
                
                // Fallback: try to find any reasonable CPU value in the line
                for (i in 7..10) {
                    if (i < parts.size) {
                        val cpuPercent = parts[i].replace("%", "").toDoubleOrNull()
                        if (cpuPercent != null && cpuPercent >= 0 && cpuPercent <= 400) {
                            logger.info("Found CPU: $cpuPercent% at position $i (fallback)")
                            return CpuMetrics(cpuPercent)
                        }
                    }
                }
                
                logger.warn("Could not parse CPU from line: $line")
                logger.warn("Parts: ${parts.joinToString(", ")}")
            }
        }
        
        logger.warn("Package $packageName (or ${packagePrefix}+) not found in top output")
        return CpuMetrics.empty()
    }
    
    
    // ==================== Flashlight-inspired ADB methods ====================
    
    private var cachedRefreshRate: Int? = null
    
    /**
     * Detect device refresh rate using dumpsys display.
     * Based on Flashlight's detectDeviceRefreshRate method.
     */
    fun detectDeviceRefreshRate(): Int {
        return try {
            val output = AdbShell.exec("shell", "dumpsys", "display")
            
            // Try renderFrameRate first
            val renderMatch = Regex("""renderFrameRate\s+(\d+\.?\d*)""").find(output)
            if (renderMatch != null) {
                return renderMatch.groupValues[1].toFloat().toInt()
            }
            
            // Fallback: extract all fps values, take highest
            val fpsMatches = Regex("""fps=(\d+\.?\d*)""").findAll(output)
            val rates = fpsMatches.map { it.groupValues[1].toFloat() }.sortedDescending().toList()
            rates.firstOrNull()?.toInt() ?: 60
        } catch (e: Exception) {
            logger.warn("Failed to detect refresh rate: ${e.message}, defaulting to 60Hz")
            60
        }
    }
    
    /**
     * Get cached device refresh rate (detects once, then caches).
     */
    fun getDeviceRefreshRate(): Int {
        if (cachedRefreshRate == null) {
            cachedRefreshRate = detectDeviceRefreshRate()
            logger.info("Detected device refresh rate: ${cachedRefreshRate}Hz")
        }
        return cachedRefreshRate!!
    }
    
    /**
     * Collect FPS using Flashlight's gfxinfo algorithm.
     * Accounts for idle time and device refresh rate.
     * 
     * @param timeIntervalMs Time interval for measurement (default: 500ms)
     * @return FPS value
     */
    fun collectFlashlightFps(timeIntervalMs: Long = 500): Double {
        return try {
            val refreshRate = getDeviceRefreshRate()
            val targetFrameTime = 1000.0 / refreshRate
            
            val output = AdbShell.exec("shell", "dumpsys", "gfxinfo", packageName)
            
            // Parse frame times from gfxinfo
            val frameTimes = mutableListOf<Double>()
            val lines = output.lines()
            var inFrameData = false
            
            for (line in lines) {
                if (line.contains("Frame timings")) {
                    inFrameData = true
                    continue
                }
                if (inFrameData && line.trim().isEmpty()) {
                    break
                }
                if (inFrameData) {
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        try {
                            val frameTime = parts[2].toDoubleOrNull()
                            if (frameTime != null && frameTime > 0) {
                                frameTimes.add(frameTime)
                            }
                        } catch (e: Exception) {
                            // Skip invalid lines
                        }
                    }
                }
            }
            
            if (frameTimes.isEmpty()) {
                return 60.0 // Default
            }
            
            // Calculate render time and idle time
            val renderTime = frameTimes.sumOf { maxOf(it, targetFrameTime) }
            val idleTime = maxOf(timeIntervalMs - renderTime, 0.0)
            val idleFrameCount = (idleTime * refreshRate) / 1000.0
            
            // Calculate FPS
            ((frameTimes.size + idleFrameCount) / (renderTime + idleTime)) * 1000.0
        } catch (e: Exception) {
            logger.warn("Failed to collect Flashlight FPS: ${e.message}")
            60.0
        }
    }
    
    /**
     * Collect CPU info using dumpsys cpuinfo as an alternative to top.
     * More reliable for some devices.
     */
    /**
     * Collect CPU info using dumpsys cpuinfo as an alternative to top.
     * More reliable for some devices.
     */
    fun collectCpuInfoFromDumpsys(): CpuMetrics {
        return try {
            val output = AdbShell.exec("shell", "dumpsys", "cpuinfo")
            
            // Strategy 1: Look for exact package match
            // Format: "0.1% 1234/com.package.name: 0.1% user + 0% kernel"
            // Regex: Start of line or space, digits% space PID/package
            val pattern = Regex("""(\d+(?:\.\d+)?)%\s+\d+/${Regex.escape(packageName)}""")
            val match = pattern.find(output)
            
            if (match != null) {
                val cpuPercent = match.groupValues[1].toDoubleOrNull() ?: 0.0
                return CpuMetrics(cpuPercent)
            }
            
            // Strategy 2: Look for package name only (sometimes PID is missing or format differs)
            // Format: "  0.1% com.package.name"
            val loosePattern = Regex("""(\d+(?:\.\d+)?)%\s+.*${Regex.escape(packageName)}""")
            val looseMatch = loosePattern.find(output)
            
            if (looseMatch != null) {
                val cpuPercent = looseMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                return CpuMetrics(cpuPercent)
            }
            
            logger.debug("Could not find CPU usage for $packageName in dumpsys cpuinfo")
            CpuMetrics.empty()
            
        } catch (e: Exception) {
            logger.warn("Failed to collect CPU from dumpsys: ${e.message}")
            CpuMetrics.empty()
        }
    }
    
    /**
     * Collect enhanced gfxinfo with histogram data.
     * Based on Flashlight's enhanced gfxinfo parsing.
     */
    fun collectEnhancedGfxInfo(): EnhancedGfxInfoMetrics {
        return try {
            val output = AdbShell.exec("shell", "dumpsys", "gfxinfo", packageName, "framestats")
            
            val frameTimes = mutableListOf<Double>()
            val histogram = mutableMapOf<String, Int>()
            
            // Parse frame times
            val lines = output.lines()
            var inFrameData = false
            
            for (line in lines) {
                if (line.contains("---PROFILEDATA---")) {
                    inFrameData = true
                    continue
                }
                if (inFrameData && line.trim().isEmpty()) {
                    break
                }
                if (inFrameData && line.startsWith("0,")) {
                    val parts = line.split(",")
                    if (parts.size >= 13) {
                        try {
                            val frameTime = (parts[13].toLong() - parts[1].toLong()) / 1_000_000.0
                            if (frameTime > 0) {
                                frameTimes.add(frameTime)
                            }
                        } catch (e: Exception) {
                            // Skip invalid lines
                        }
                    }
                }
            }
            
            // Build histogram
            for (frameTime in frameTimes) {
                val bucket = when {
                    frameTime < 8 -> "<8ms"
                    frameTime < 16 -> "8-16ms"
                    frameTime < 32 -> "16-32ms"
                    frameTime < 64 -> "32-64ms"
                    else -> ">64ms"
                }
                histogram[bucket] = histogram.getOrDefault(bucket, 0) + 1
            }
            
            EnhancedGfxInfoMetrics(
                frameTimes = frameTimes,
                histogram = histogram,
                totalFrames = frameTimes.size
            )
        } catch (e: Exception) {
            logger.warn("Failed to collect enhanced gfxinfo: ${e.message}")
            EnhancedGfxInfoMetrics(emptyList(), emptyMap(), 0)
        }
    }
}

data class GfxInfoMetrics(
    val jankyFrames: Int,
    val totalFrames: Int,
    val avgFrameTimeMs: Double,
    val p50FrameTimeMs: Double,
    val p90FrameTimeMs: Double,
    val p99FrameTimeMs: Double
) {
    val jankPercentage: Double
        get() = if (totalFrames > 0) (jankyFrames.toDouble() / totalFrames) * 100.0 else 0.0
    
    val fps: Double
        get() = if (avgFrameTimeMs > 0) 1000.0 / avgFrameTimeMs else 60.0
    
    companion object {
        fun empty() = GfxInfoMetrics(0, 0, 0.0, 0.0, 0.0, 0.0)
    }
}

data class MemInfoMetrics(
    val rssMb: Int,
    val javaHeapMb: Int,
    val graphicsMb: Int
) {
    companion object {
        fun empty() = MemInfoMetrics(0, 0, 0)
    }
}

data class CpuMetrics(
    val cpuPercentage: Double
) {
    companion object {
        fun empty() = CpuMetrics(0.0)
    }
}

/**
 * Enhanced gfxinfo metrics with histogram data.
 * Based on Flashlight's enhanced gfxinfo parsing.
 */
data class EnhancedGfxInfoMetrics(
    val frameTimes: List<Double>,
    val histogram: Map<String, Int>,
    val totalFrames: Int
) {
    val avgFrameTime: Double
        get() = if (frameTimes.isNotEmpty()) frameTimes.average() else 0.0
    
    val p50FrameTime: Double
        get() = if (frameTimes.isNotEmpty()) frameTimes.sorted()[frameTimes.size / 2] else 0.0
    
    val p90FrameTime: Double
        get() = if (frameTimes.isNotEmpty()) frameTimes.sorted()[(frameTimes.size * 0.9).toInt()] else 0.0
    
    val p99FrameTime: Double
        get() = if (frameTimes.isNotEmpty()) frameTimes.sorted()[(frameTimes.size * 0.99).toInt()] else 0.0
    
    companion object {
        fun empty() = EnhancedGfxInfoMetrics(emptyList(), emptyMap(), 0)
    }
}

