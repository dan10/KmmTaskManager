package com.danioliveira.appium.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Centralized ADB command execution utility.
 * 
 * Provides robust ADB command execution with:
 * - Automatic ADB path discovery
 * - Retry logic for flaky commands
 * - Timeout handling
 * - Detailed error logging
 * - stderr/stdout separation
 */
object AdbShell {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var cachedAdbPath: String? = null
    
    /**
     * Execute an ADB command with retries and timeout.
     * 
     * @param args ADB command arguments (e.g., "shell", "dumpsys", "meminfo", "com.example.app")
     * @param retries Number of retry attempts for flaky commands (default: 0)
     * @param timeoutSeconds Timeout in seconds (default: 30)
     * @return Command stdout output
     * @throws AdbException if command fails after all retries
     */
    fun exec(
        vararg args: String,
        retries: Int = 0,
        timeoutSeconds: Long = 30
    ): String {
        var lastException: Exception? = null
        
        repeat(retries + 1) { attempt ->
            try {
                return execInternal(args.toList(), timeoutSeconds)
            } catch (e: Exception) {
                lastException = e
                if (attempt < retries) {
                    logger.warn("ADB command failed (attempt ${attempt + 1}/${retries + 1}): ${e.message}")
                    Thread.sleep(500) // Brief delay before retry
                }
            }
        }
        
        throw AdbException(
            "ADB command failed after ${retries + 1} attempts: ${args.joinToString(" ")}",
            lastException
        )
    }
    
    /**
     * Execute an ADB command without retries (for commands that should fail fast).
     */
    fun execNoRetry(vararg args: String, timeoutSeconds: Long = 30): String {
        return execInternal(args.toList(), timeoutSeconds)
    }
    
    /**
     * Execute an ADB command with stdin input.
     * Useful for passing data to commands without writing to files.
     * 
     * @param stdin String to write to command's stdin
     * @param args ADB command arguments
     * @param timeoutSeconds Timeout in seconds (default: 30)
     * @return Command stdout output
     * @throws AdbException if command fails
     */
    fun execWithStdin(
        stdin: String,
        vararg args: String,
        timeoutSeconds: Long = 30
    ): String {
        val adbPath = getAdbPath()
        val command = listOf(adbPath) + args
        
        logger.debug("Executing with stdin: ${command.joinToString(" ")}")
        logger.debug("Stdin length: ${stdin.length} bytes")
        
        val process = ProcessBuilder(command)
            .redirectErrorStream(false) // Keep stderr separate
            .start()
        
        // Write stdin in a separate thread
        val stdinThread = Thread {
            try {
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write(stdin)
                    writer.flush()
                }
            } catch (e: Exception) {
                logger.warn("Failed to write stdin: ${e.message}")
            }
        }
        stdinThread.start()
        
        // Read stdout and stderr concurrently
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        
        val stdoutThread = Thread {
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { stdout.appendLine(it) }
            }
        }
        
        val stderrThread = Thread {
            process.errorStream.bufferedReader().use { reader ->
                reader.forEachLine { stderr.appendLine(it) }
            }
        }
        
        stdoutThread.start()
        stderrThread.start()
        
        // Wait for stdin to be written
        stdinThread.join(5000) // Max 5s for stdin write
        
        // Wait for process with timeout
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        
        if (!completed) {
            process.destroyForcibly()
            throw AdbTimeoutException("ADB command timed out after ${timeoutSeconds}s: ${args.joinToString(" ")}")
        }
        
        stdoutThread.join()
        stderrThread.join()
        
        val exitCode = process.exitValue()
        val stdoutStr = stdout.toString()
        val stderrStr = stderr.toString()
        
        if (exitCode != 0) {
            logger.error("ADB command failed with exit code $exitCode")
            logger.error("Command: ${args.joinToString(" ")}")
            logger.error("Stderr: $stderrStr")
            
            throw AdbCommandFailedException(
                "ADB command failed with exit code $exitCode: ${args.joinToString(" ")}",
                exitCode,
                stdoutStr,
                stderrStr
            )
        }
        
        if (stderrStr.isNotBlank()) {
            logger.debug("ADB stderr (non-fatal): $stderrStr")
        }
        
        return stdoutStr
    }
    
    /**
     * Check if a device is connected.
     */
    fun isDeviceConnected(): Boolean {
        return try {
            val output = execNoRetry("devices", timeoutSeconds = 5)
            output.lines().any { line ->
                line.contains("\tdevice") || line.contains("\tauthorizing")
            }
        } catch (e: Exception) {
            logger.warn("Failed to check device connection: ${e.message}")
            false
        }
    }
    
    /**
     * Get the connected device serial.
     */
    fun getDeviceSerial(): String? {
        return try {
            val output = execNoRetry("devices", timeoutSeconds = 5)
            output.lines()
                .firstOrNull { it.contains("\tdevice") }
                ?.split("\t")
                ?.firstOrNull()
        } catch (e: Exception) {
            logger.warn("Failed to get device serial: ${e.message}")
            null
        }
    }
    
    /**
     * Get Android API level of the connected device.
     */
    fun getApiLevel(): Int {
        return try {
            val output = execNoRetry("shell", "getprop", "ro.build.version.sdk")
            output.trim().toIntOrNull() ?: 0
        } catch (e: Exception) {
            logger.warn("Failed to get API level: ${e.message}")
            0
        }
    }
    
    private fun execInternal(args: List<String>, timeoutSeconds: Long): String {
        val adbPath = getAdbPath()
        val command = listOf(adbPath) + args
        
        logger.debug("Executing: ${command.joinToString(" ")}")
        
        val process = ProcessBuilder(command)
            .redirectErrorStream(false) // Keep stderr separate
            .start()
        
        // Read stdout and stderr concurrently
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        
        val stdoutThread = Thread {
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { stdout.appendLine(it) }
            }
        }
        
        val stderrThread = Thread {
            process.errorStream.bufferedReader().use { reader ->
                reader.forEachLine { stderr.appendLine(it) }
            }
        }
        
        stdoutThread.start()
        stderrThread.start()
        
        // Wait for process with timeout
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        
        if (!completed) {
            process.destroyForcibly()
            throw AdbTimeoutException("ADB command timed out after ${timeoutSeconds}s: ${args.joinToString(" ")}")
        }
        
        stdoutThread.join()
        stderrThread.join()
        
        val exitCode = process.exitValue()
        val stdoutStr = stdout.toString()
        val stderrStr = stderr.toString()
        
        if (exitCode != 0) {
            logger.error("ADB command failed with exit code $exitCode")
            logger.error("Command: ${args.joinToString(" ")}")
            logger.error("Stderr: $stderrStr")
            
            throw AdbCommandFailedException(
                "ADB command failed with exit code $exitCode: ${args.joinToString(" ")}",
                exitCode,
                stdoutStr,
                stderrStr
            )
        }
        
        if (stderrStr.isNotBlank()) {
            logger.debug("ADB stderr (non-fatal): $stderrStr")
        }
        
        return stdoutStr
    }
    
    private fun getAdbPath(): String {
        // Return cached path if available
        cachedAdbPath?.let { return it }
        
        // Try common locations for ADB
        val possiblePaths = listOf(
            "adb", // Already in PATH
            "/usr/local/bin/adb",
            System.getenv("ANDROID_HOME")?.let { "$it/platform-tools/adb" },
            System.getenv("ANDROID_SDK_ROOT")?.let { "$it/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Library/Android/sdk/platform-tools/adb" },
            System.getProperty("user.home")?.let { "$it/Android/Sdk/platform-tools/adb" },
            "/opt/android-sdk/platform-tools/adb"
        ).filterNotNull()
        
        for (path in possiblePaths) {
            try {
                val process = ProcessBuilder(path, "version")
                    .redirectErrorStream(true)
                    .start()
                
                val completed = process.waitFor(5, TimeUnit.SECONDS)
                if (completed && process.exitValue() == 0) {
                    logger.info("Found ADB at: $path")
                    cachedAdbPath = path
                    return path
                }
            } catch (e: Exception) {
                // Try next path
                continue
            }
        }
        
        throw AdbNotFoundException(
            "ADB not found! Please ensure Android SDK is installed and ANDROID_HOME is set.\n" +
            "Tried paths: ${possiblePaths.joinToString(", ")}"
        )
    }
    
    /**
     * Reset the cached ADB path (useful for testing or if ADB location changes).
     */
    fun resetCache() {
        cachedAdbPath = null
    }
}

/**
 * Base exception for ADB-related errors.
 */
open class AdbException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when ADB executable is not found.
 */
class AdbNotFoundException(message: String) : AdbException(message)

/**
 * Thrown when an ADB command times out.
 */
class AdbTimeoutException(message: String) : AdbException(message)

/**
 * Thrown when an ADB command fails with a non-zero exit code.
 */
class AdbCommandFailedException(
    message: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) : AdbException(message)

