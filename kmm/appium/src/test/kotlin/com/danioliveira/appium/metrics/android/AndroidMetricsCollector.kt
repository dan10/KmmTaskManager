package com.danioliveira.appium.metrics.android

import org.slf4j.LoggerFactory
import java.io.File

class AndroidMetricsCollector(private val packageName: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var perfettoProcess: Process? = null
    
    fun resetGfxInfo() {
        try {
            execAdbCommand("shell", "dumpsys", "gfxinfo", packageName, "reset")
            logger.debug("Reset gfxinfo for $packageName")
        } catch (e: Exception) {
            logger.warn("Failed to reset gfxinfo: ${e.message}")
        }
    }
    
    fun collectGfxInfo(): GfxInfoMetrics {
        return try {
            val output = execAdbCommand("shell", "dumpsys", "gfxinfo", packageName)
            parseGfxInfo(output)
        } catch (e: Exception) {
            logger.error("Failed to collect gfxinfo: ${e.message}")
            GfxInfoMetrics.empty()
        }
    }
    
    fun collectMemInfo(): MemInfoMetrics {
        return try {
            val output = execAdbCommand("shell", "dumpsys", "meminfo", packageName)
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
            val output = execAdbCommand("shell", "top", "-n", "1", "-p", pid.toString())
            
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
            execAdbCommand("shell", "am", "force-stop", packageName)
            Thread.sleep(1000)
            
            // Start the app and measure
            execAdbCommand("shell", "am", "start", "-W", "-n", 
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
    
    private fun getPid(): Int? {
        return try {
            val output = execAdbCommand("shell", "pidof", packageName)
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
        // Try common locations for ADB
        val possiblePaths = listOf(
            "adb", // Already in PATH
            "/usr/local/bin/adb",
            System.getenv("ANDROID_HOME")?.let { "$it/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Library/Android/sdk/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Android/Sdk/platform-tools/adb" },
            "/opt/android-sdk/platform-tools/adb"
        ).filterNotNull()
        
        for (path in possiblePaths) {
            try {
                val process = ProcessBuilder(path, "version")
                    .redirectErrorStream(true)
                    .start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    logger.info("Found ADB at: $path")
                    return path
                }
            } catch (e: Exception) {
                // Try next path
                continue
            }
        }
        
        logger.error("⚠️  ADB not found! Please ensure Android SDK is installed and ANDROID_HOME is set.")
        logger.error("   Metrics will return zeros. Install: https://developer.android.com/studio/command-line/adb")
        throw IllegalStateException("ADB not found in PATH or common locations. Set ANDROID_HOME environment variable.")
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

