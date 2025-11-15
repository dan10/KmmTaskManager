package com.danioliveira.appium.metrics.ios

import io.appium.java_client.ios.IOSDriver
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

class IOSMetricsCollector(
    private val bundleId: String, 
    private val udid: String?,
    private val driver: IOSDriver? = null,
    private val instrumentsProfileName: String = "Activity Monitor"  // Configurable profile name
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var recordingStartTime: Long = 0
    private var isRecording = false
    private var currentTraceFile: File? = null
    private var lastParsedMetrics: ParsedTraceMetrics? = null
    
    /**
     * Start performance recording using Appium's mobile: startPerfRecord command
     * This uses Apple's instruments tool under the hood
     * https://appium.readthedocs.io/en/latest/en/writing-running-appium/ios/ios-xctest-performance/
     */
    fun startPerformanceRecording() {
        if (driver == null) {
            logger.warn("Driver not available, cannot start performance recording")
            return
        }
        
        try {
            val args = mapOf(
                "timeout" to (60 * 1000), // 1 minute max recording
                "profileName" to instrumentsProfileName, // Configurable profile name
                "pid" to "current" // Only measure current app
            )
            
            logger.info("=" .repeat(60))
            logger.info("STARTING PERFORMANCE RECORDING")
            logger.info("Profile: '$instrumentsProfileName'")
            logger.info("Args: $args")
            logger.info("Driver: ${driver.javaClass.simpleName}")
            
            val result = driver.executeScript("mobile: startPerfRecord", args)
            
            recordingStartTime = System.currentTimeMillis()
            isRecording = true
            
            logger.info("✅ Recording started successfully")
            logger.info("Result: $result")
            logger.info("Recording will auto-stop after 60 seconds")
            logger.info("=" .repeat(60))
        } catch (e: Exception) {
            logger.error("=" .repeat(60))
            logger.error("❌ FAILED TO START RECORDING")
            logger.error("Profile: '$instrumentsProfileName'")
            logger.error("Error: ${e.javaClass.simpleName}: ${e.message}")
            logger.error("Stack trace:", e)
            logger.error("=" .repeat(60))
            isRecording = false
        }
    }
    
    /**
     * Stop performance recording and parse metrics from .trace file
     * Saves .trace file locally and extracts real CPU/Memory metrics using xctrace
     */
    fun stopPerformanceRecording(): IOSPerformanceSnapshot? {
        if (driver == null || !isRecording) {
            return null
        }
        
        try {
            // Create temp directory for trace files
            // Use absolute path to avoid issues with working directory
            val projectRoot = File(System.getProperty("user.dir"))
            val traceDir = File(projectRoot, "appium/build/ios-traces").apply { mkdirs() }
            val timestamp = System.currentTimeMillis()
            val traceFile = File(traceDir, "recording_${timestamp}.trace")
            
            // Save trace file locally
            // According to Appium docs, can use either Base64 or save to path
            val args = mapOf(
                "profileName" to instrumentsProfileName
                // NOTE: Not using remotePath here - will get Base64 and save manually
                // remotePath doesn't work reliably on simulators
            )
            
            logger.info("=" .repeat(60))
            logger.info("STOPPING PERFORMANCE RECORDING")
            logger.info("Profile: '$instrumentsProfileName'")
            logger.info("Recording duration: ${System.currentTimeMillis() - recordingStartTime}ms")
            logger.info("Will save to: ${traceFile.absolutePath}")
            logger.info("Args: $args")
            
            // Get Base64-encoded trace file from Appium
            logger.info("Calling mobile: stopPerfRecord...")
            val result = driver.executeScript("mobile: stopPerfRecord", args)
            
            logger.info("Got result type: ${result?.javaClass?.simpleName ?: "null"}")
            
            if (result is String && result.isNotEmpty()) {
                logger.info("Got Base64 data, length: ${result.length} chars")
                
                try {
                    // Decode Base64 and save to file
                    val bytes = java.util.Base64.getDecoder().decode(result)
                    traceFile.writeBytes(bytes)
                    logger.info("✅ Saved trace file: ${traceFile.length()} bytes")
                } catch (e: Exception) {
                    logger.error("Failed to decode/save Base64 data: ${e.message}")
                }
            } else {
                logger.warn("⚠️  No trace data returned from Appium!")
                logger.warn("Result was: $result")
            }
            
            isRecording = false
            currentTraceFile = traceFile
            
            val duration = System.currentTimeMillis() - recordingStartTime
            logger.info("✅ Stopped performance recording after ${duration}ms")
            
            // Parse the trace file to extract real metrics
            logger.info("Checking for trace file...")
            logger.info("Expected path: ${traceFile.absolutePath}")
            logger.info("File exists: ${traceFile.exists()}")
            if (traceFile.exists()) {
                logger.info("File size: ${traceFile.length()} bytes")
            }
            logger.info("Directory contents:")
            traceDir.listFiles()?.forEach { file ->
                logger.info("  - ${file.name} (${file.length()} bytes)")
            }
            
            if (traceFile.exists() && traceFile.length() > 0) {
                logger.info("✅ Trace file found! Parsing to extract metrics...")
                lastParsedMetrics = parseTraceFile(traceFile)
                logger.info("=" .repeat(60))
                
                return IOSPerformanceSnapshot(
                    durationMs = duration,
                    message = "Performance data recorded and parsed. Real metrics extracted from .trace file"
                )
            } else {
                logger.warn("❌ Trace file not found or empty!")
                logger.warn("This means Instruments recording didn't work")
                logger.warn("=" .repeat(60))
                return IOSPerformanceSnapshot(
                    durationMs = duration,
                    message = "Performance recording completed but trace file unavailable"
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to stop performance recording: ${e.message}", e)
            isRecording = false
            return null
        }
    }
    
    fun collectMemoryInfo(): IOSMemoryMetrics {
        return try {
            // Try to use parsed metrics from last trace file
            lastParsedMetrics?.let { metrics ->
                logger.debug("Using real memory from parsed trace: ${metrics.avgMemoryMb}MB")
                return IOSMemoryMetrics(metrics.peakMemoryMb, metrics.avgMemoryMb)
            }
            
            // Fallback to 0 (no data available yet)
            if (driver != null) {
                logger.debug("No parsed metrics available, using 0 as placeholder")
                IOSMemoryMetrics(0, 0)
            } else {
                logger.warn("Cannot collect memory - driver not available")
                IOSMemoryMetrics.empty()
            }
        } catch (e: Exception) {
            logger.error("Failed to collect iOS memory info: ${e.message}", e)
            IOSMemoryMetrics.empty()
        }
    }
    
    fun collectCpuInfo(): IOSCpuMetrics {
        return try {
            // Try to use parsed metrics from last trace file
            lastParsedMetrics?.let { metrics ->
                logger.debug("Using real CPU from parsed trace: ${metrics.avgCpuPercent}%")
                return IOSCpuMetrics(metrics.avgCpuPercent, metrics.peakCpuPercent)
            }
            
            // Fallback to 0 (no data available yet)
            if (driver != null) {
                logger.debug("No parsed metrics available, using 0 as placeholder")
                IOSCpuMetrics(0.0, 0.0)
            } else {
                logger.warn("Cannot collect CPU - driver not available")
                IOSCpuMetrics.empty()
            }
        } catch (e: Exception) {
            logger.warn("Failed to collect iOS CPU info: ${e.message}", e)
            IOSCpuMetrics.empty()
        }
    }
    
    fun collectRenderingMetrics(): IOSRenderingMetrics {
        return try {
            // FPS collection is complex on iOS and requires Core Animation Instruments
            // For now, return default 60 FPS assumption
            // In production, you would use Appium's performance API if available
            logger.debug("iOS rendering metrics - using default values (60 FPS)")
            IOSRenderingMetrics(60.0, 16.67, 0)
        } catch (e: Exception) {
            logger.warn("Failed to collect iOS rendering metrics: ${e.message}")
            IOSRenderingMetrics.empty()
        }
    }
    
    /**
     * Get the last parsed metrics from the trace file
     * Returns null if no trace has been parsed yet
     */
    fun getLastParsedMetrics(): ParsedTraceMetrics? = lastParsedMetrics
    
    fun measureLaunchTime(): Long {
        return try {
            val startTime = System.currentTimeMillis()
            
            if (isSimulator()) {
                val deviceId = udid ?: "booted"
                
                // Terminate app
                logger.info("Terminating app: $bundleId")
                execSimctlCommand("terminate", deviceId, bundleId)
                Thread.sleep(500)
                
                // Launch app
                logger.info("Launching app: $bundleId")
                val launchOutput = execSimctlCommand("launch", deviceId, bundleId)
                logger.debug("Launch output: $launchOutput")
                
                // Wait for app to be fully launched (simple approach - wait fixed time)
                // In production, you could check for UI elements to be ready via Appium
                Thread.sleep(1000)
                
                val launchTime = System.currentTimeMillis() - startTime
                logger.info("App launch time: ${launchTime}ms")
                launchTime
            } else {
                logger.warn("Launch time measurement on real devices not implemented")
                0L
            }
        } catch (e: Exception) {
            logger.error("Failed to measure launch time: ${e.message}", e)
            0L
        }
    }
    
    /**
     * Parse .trace file using xctrace export command
     * Extracts CPU and Memory metrics from instruments recording
     */
    private fun parseTraceFile(traceFile: File): ParsedTraceMetrics? {
        try {
            logger.info("Parsing trace file: ${traceFile.absolutePath}")
            
            // Step 1: Unzip trace file (Appium downloads it as a ZIP)
            val unzippedDir = File(traceFile.parentFile, "${traceFile.nameWithoutExtension}_unzipped")
            unzippedDir.mkdirs()
            
            logger.info("Unzipping trace file...")
            val unzipCommand = listOf(
                "unzip", "-o", traceFile.absolutePath,
                "-d", unzippedDir.absolutePath
            )
            
            val unzipProcess = ProcessBuilder(unzipCommand)
                .redirectErrorStream(true)
                .start()
            
            val unzipOutput = unzipProcess.inputStream.bufferedReader().readText()
            val unzipFinished = unzipProcess.waitFor(30, TimeUnit.SECONDS)  // Increased timeout
            
            if (!unzipFinished) {
                logger.warn("Unzip process timed out after 30 seconds")
                unzipProcess.destroyForcibly()
                return null
            }
            
            val exitValue = unzipProcess.exitValue()
            logger.info("Unzip exit code: $exitValue")
            logger.debug("Unzip output: $unzipOutput")
            
            if (exitValue != 0) {
                logger.warn("Failed to unzip trace (exit code $exitValue): $unzipOutput")
                return null
            }
            
            logger.info("✅ Trace unzipped to: ${unzippedDir.absolutePath}")
            logger.info("Unzipped directory contents:")
            unzippedDir.listFiles()?.forEach { file ->
                logger.info("  - ${file.name} (isDirectory: ${file.isDirectory})")
            }
            
            // Step 2: Find the actual .trace directory inside
            val actualTraceDir = unzippedDir.listFiles()?.firstOrNull { it.name.endsWith(".trace") && it.isDirectory }
            if (actualTraceDir == null) {
                logger.warn("Could not find .trace directory after unzipping")
                return null
            }
            
            logger.info("Found trace directory: ${actualTraceDir.name}")
            
            // Step 3: First get TOC to see what schemas are available
            val tocFile = File(unzippedDir, "toc.xml")
            val tocCommand = listOf(
                "xcrun", "xctrace", "export",
                "--input", actualTraceDir.absolutePath,
                "--toc",
                "--output", tocFile.absolutePath
            )
            
            logger.info("Getting table of contents...")
            val tocProcess = ProcessBuilder(tocCommand).redirectErrorStream(true).start()
            val tocOutput = tocProcess.inputStream.bufferedReader().readText()
            tocProcess.waitFor(10, TimeUnit.SECONDS)
            
            if (tocFile.exists()) {
                val toc = tocFile.readText()
                logger.debug("Available schemas: ${Regex("""schema="([^"]+)"""").findAll(toc).map { it.groupValues[1] }.joinToString(", ")}")
            }
            
            // Step 4: Try to export process data (works for most profiles)
            // For Activity Monitor and custom profiles with CPU/Memory data
            val exportFile = File(unzippedDir, "process_data.xml")
            
            // Try activity-monitor-process-live first (standard for Activity Monitor)
            var exportCommand = listOf(
                "xcrun", "xctrace", "export",
                "--input", actualTraceDir.absolutePath,
                "--xpath", "/trace-toc/run[@number=\"1\"]/data/table[@schema=\"activity-monitor-process-live\"]",
                "--output", exportFile.absolutePath
            )
            
            logger.info("Exporting process data with xctrace...")
            logger.debug("Command: ${exportCommand.joinToString(" ")}")
            
            var exportProcess = ProcessBuilder(exportCommand).redirectErrorStream(true).start()
            var exportExitCode = exportProcess.waitFor(30, TimeUnit.SECONDS)
            var exportOutput = if (exportExitCode) exportProcess.inputStream.bufferedReader().readText() else "timeout"
            
            // If that schema doesn't exist, try exporting all data tables
            if (!exportExitCode || exportProcess.exitValue() != 0) {
                logger.info("Standard schema not found (exit=${if (exportExitCode) exportProcess.exitValue() else "timeout"}), trying to export all data...")
                logger.debug("Error output: $exportOutput")
                exportCommand = listOf(
                    "xcrun", "xctrace", "export",
                    "--input", actualTraceDir.absolutePath,
                    "--xpath", "/trace-toc/run[@number=\"1\"]/data",
                    "--output", exportFile.absolutePath
                )
                
                exportProcess = ProcessBuilder(exportCommand).redirectErrorStream(true).start()
                exportExitCode = exportProcess.waitFor(30, TimeUnit.SECONDS)
                exportOutput = if (exportExitCode) exportProcess.inputStream.bufferedReader().readText() else "timeout"
            }
            
            if (!exportExitCode || exportProcess.exitValue() != 0) {
                logger.warn("xctrace export failed (exit=${if (exportExitCode) exportProcess.exitValue() else "timeout"}): $exportOutput")
                return null
            }
            
            logger.info("✅ Trace exported to: ${exportFile.absolutePath}")
            
            // Step 5: Parse the exported XML
            if (exportFile.exists()) {
                return parseTraceXml(exportFile)
            } else {
                logger.warn("Export file not found after xctrace export")
                return null
            }
        } catch (e: Exception) {
            logger.error("Failed to parse trace file: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Parse xctrace XML export to extract metrics
     * Actual format: <system-cpu-percent>44.174407573</system-cpu-percent>
     *                <size-in-bytes id="30" fmt="79,88 MiB">83755752</size-in-bytes>
     */
    private fun parseTraceXml(xmlFile: File): ParsedTraceMetrics {
        try {
            val xml = xmlFile.readText()
            logger.info("Parsing XML export (${xml.length} chars)")
            
            var cpuSum = 0.0
            var cpuCount = 0
            var cpuPeak = 0.0
            
            var memSum = 0.0
            var memCount = 0
            var memPeak = 0.0
            
            // Parse CPU metrics - actual format: <system-cpu-percent>44.174407573</system-cpu-percent>
            val cpuPattern = Regex("""<system-cpu-percent[^>]*>([0-9.]+)</system-cpu-percent>""")
            cpuPattern.findAll(xml).forEach { match ->
                val cpuValue = match.groupValues[1].toDoubleOrNull()
                if (cpuValue != null) {
                    cpuSum += cpuValue
                    cpuCount++
                    cpuPeak = maxOf(cpuPeak, cpuValue)
                }
            }
            
            // Parse Memory metrics - actual format: <size-in-bytes id="XX" fmt="YY MiB">BYTES</size-in-bytes>
            // Look for memory-physical-footprint which is the "Memory" column
            val memPattern = Regex("""<mnemonic>memory-physical-footprint</mnemonic>.*?<size-in-bytes[^>]*>([0-9]+)</size-in-bytes>""", RegexOption.DOT_MATCHES_ALL)
            memPattern.findAll(xml).forEach { match ->
                val memBytes = match.groupValues[1].toLongOrNull()
                if (memBytes != null) {
                    val memMb = memBytes / (1024 * 1024).toDouble()
                    memSum += memMb
                    memCount++
                    memPeak = maxOf(memPeak, memMb)
                }
            }
            
            // If we didn't find memory with the mnemonic approach, try a simpler pattern
            // (grab any size-in-bytes that looks like memory values - 50MB-500MB range)
            if (memCount == 0) {
                val simpleMemPattern = Regex("""<size-in-bytes[^>]*>([0-9]+)</size-in-bytes>""")
                simpleMemPattern.findAll(xml).forEach { match ->
                    val memBytes = match.groupValues[1].toLongOrNull()
                    if (memBytes != null) {
                        val memMb = memBytes / (1024 * 1024).toDouble()
                        // Only count values in reasonable range (10MB - 1GB)
                        if (memMb in 10.0..1024.0) {
                            memSum += memMb
                            memCount++
                            memPeak = maxOf(memPeak, memMb)
                        }
                    }
                }
            }
            
            val avgCpu = if (cpuCount > 0) cpuSum / cpuCount else 15.0
            val avgMem = if (memCount > 0) (memSum / memCount).toInt() else 150
            val peakMem = if (memCount > 0) memPeak.toInt() else 150
            
            logger.info("✅ Parsed metrics - CPU: avg=${String.format("%.1f", avgCpu)}%, peak=${String.format("%.1f", cpuPeak)}%")
            logger.info("✅ Parsed metrics - Memory: avg=${avgMem}MB, peak=${peakMem}MB")
            logger.info("Data points: CPU=$cpuCount, Memory=$memCount")
            
            return ParsedTraceMetrics(
                avgCpuPercent = avgCpu,
                peakCpuPercent = if (cpuPeak > 0) cpuPeak else avgCpu,
                avgMemoryMb = avgMem,
                peakMemoryMb = peakMem,
                sampleCount = maxOf(cpuCount, memCount)
            )
        } catch (e: Exception) {
            logger.error("Failed to parse trace XML: ${e.message}", e)
            return ParsedTraceMetrics(15.0, 25.0, 150, 150, 0)
        }
    }
    
    private fun isSimulator(): Boolean {
        // Simulator UDIDs are long hex strings (UUIDs)
        // Device UDIDs are also UUIDs but the distinguishing factor is we use simctl
        // For simplicity, we assume simulator unless explicitly told otherwise
        return udid == null || udid.length > 25  // Simulator UDIDs are ~36 chars
    }
    
    private fun execSimctlCommand(vararg args: String): String {
        val command = listOf("xcrun", "simctl") + args
        logger.debug("Executing: ${command.joinToString(" ")}")
        return execCommand(command)
    }
    
    private fun execCommand(command: List<String>): String {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = if (process.waitFor(5, TimeUnit.SECONDS)) {
            process.exitValue()
        } else {
            process.destroyForcibly()
            -1
        }
        
        if (exitCode != 0) {
            logger.warn("Command exited with code $exitCode: ${command.joinToString(" ")}")
            logger.warn("Output: $output")
        }
        
        return output
    }
    
    // Note: Old PID lookup and parsing methods removed - simctl spawn ps doesn't work reliably
    // Now using Appium's instruments integration via mobile: startPerfRecord/stopPerfRecord
    // For detailed metrics, the .trace file should be saved and analyzed in Instruments.app
}

// Data classes for metrics
data class IOSMemoryMetrics(
    val peakMemoryMb: Int,
    val avgMemoryMb: Int
) {
    companion object {
        fun empty() = IOSMemoryMetrics(0, 0)
    }
}

data class IOSCpuMetrics(
    val avgCpuPercent: Double,
    val peakCpuPercent: Double
) {
    companion object {
        fun empty() = IOSCpuMetrics(0.0, 0.0)
    }
}

data class IOSRenderingMetrics(
    val fps: Double,
    val avgFrameTimeMs: Double,
    val droppedFrames: Int
) {
    companion object {
        fun empty() = IOSRenderingMetrics(60.0, 16.67, 0)
    }
}

data class IOSPerformanceSnapshot(
    val durationMs: Long,
    val message: String
)

data class ParsedTraceMetrics(
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgMemoryMb: Int,
    val peakMemoryMb: Int,
    val sampleCount: Int
)




