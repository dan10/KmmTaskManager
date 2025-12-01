package com.danioliveira.appium.metrics.android.perfetto

import androidx.benchmark.traceprocessor.ExperimentalTraceProcessorApi
import androidx.benchmark.traceprocessor.ServerLifecycleManager
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.ServerSocket

/**
 * Desktop implementation of ServerLifecycleManager for running trace_processor_shell
 * as an HTTP server on JVM (non-Android) environments.
 * 
 * This is similar to androidx.benchmark.macro.ShellServerLifecycleManager but adapted
 * for desktop/test environments where we don't have Android Shell APIs.
 */
@OptIn(ExperimentalTraceProcessorApi::class)
class DesktopServerLifecycleManager : ServerLifecycleManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private var process: Process? = null
    private var httpPort: Int = 0
    
    companion object {
        private const val SERVER_PROCESS_NAME = "trace_processor_shell"
        
        /**
         * Find the trace_processor_shell binary.
         * 
         * Searches in:
         * 1. Environment variable TRACE_PROCESSOR_PATH
         * 2. Current directory
         * 3. ~/bin
         * 4. /usr/local/bin
         */
        fun findTraceProcessorBinary(): File {
            val possiblePaths = listOf(
                System.getenv("TRACE_PROCESSOR_PATH"),
                "./trace_processor_shell",
                "${System.getProperty("user.home")}/bin/trace_processor_shell",
                "/usr/local/bin/trace_processor_shell",
                // macOS ARM
                "${System.getProperty("user.home")}/bin/trace_processor_shell-arm64",
                "/usr/local/bin/trace_processor_shell-arm64"
            ).filterNotNull()
            
            for (path in possiblePaths) {
                val file = File(path)
                if (file.exists() && file.canExecute()) {
                    return file
                }
            }
            
            throw IllegalStateException(
                "trace_processor_shell binary not found. " +
                "Please download from https://github.com/google/perfetto/releases " +
                "and set TRACE_PROCESSOR_PATH environment variable or place in ~/bin/"
            )
        }
        
        /**
         * Find a free TCP port on localhost.
         */
        private fun findFreePort(): Int {
            ServerSocket(0).use { socket ->
                socket.reuseAddress = true
                return socket.localPort
            }
        }
    }
    
    override fun start(): Int {
        if (process != null) {
            logger.warn("TraceProcessor server already running on port $httpPort")
            return httpPort
        }
        
        try {
            val binaryPath = findTraceProcessorBinary()
            val desiredPort = findFreePort()
            httpPort = desiredPort
            
            logger.info("Starting TraceProcessor server: ${binaryPath.absolutePath} (port $desiredPort)")
            
            // Start trace_processor_shell in HTTP server mode with explicit port
            process = ProcessBuilder(
                binaryPath.absolutePath,
                "-D", // Interactive mode
                "--httpd",
                "--http-port",
                desiredPort.toString()
            )
                .redirectErrorStream(true)
                .start()
            
            // Drain output in the background to avoid blocking buffers
            val outputReader = BufferedReader(InputStreamReader(process!!.inputStream))
            Thread {
                try {
                    outputReader.useLines { lines ->
                        lines.forEach { logger.debug("TraceProcessor: $it") }
                    }
                } catch (_: Exception) {
                    // Ignore reader shutdown exceptions
                }
            }.apply {
                isDaemon = true
                start()
            }
            
            // Wait a bit for server to start
            Thread.sleep(500)
            
            if (process?.isAlive != true) {
                throw IllegalStateException("TraceProcessor process exited unexpectedly")
            }
            
            logger.info("✅ TraceProcessor server started on port $httpPort")
            return httpPort
            
        } catch (e: Exception) {
            process?.destroy()
            process = null
            throw IllegalStateException("Failed to start TraceProcessor server: ${e.message}", e)
        }
    }
    
    override fun timeoutMessage(): String {
        val processRunning = process?.isAlive ?: false
        
        return if (processRunning) {
            "The test app cannot connect to the trace_processor_shell server on port $httpPort."
        } else {
            "Perfetto trace_processor_shell did not start correctly or has crashed."
        }
    }
    
    override fun stop() {
        if (process == null) {
            logger.warn("Tried to stop trace shell processor http server without starting it.")
            return
        }
        
        logger.info("Stopping TraceProcessor server (port $httpPort)")
        process?.destroy()
        
        // Wait up to 5 seconds for graceful shutdown
        val exited = process?.waitFor(5, java.util.concurrent.TimeUnit.SECONDS) ?: true
        if (!exited) {
            logger.warn("TraceProcessor did not stop gracefully, forcing shutdown")
            process?.destroyForcibly()
        }
        
        logger.info("Perfetto trace processor shell server stopped (port=$httpPort)")
        process = null
        httpPort = 0
    }
}

